package com.pushtorefresh.storio.sqlite.annotations.processor

import com.google.auto.service.AutoService
import com.pushtorefresh.storio.common.annotations.processor.ProcessingException
import com.pushtorefresh.storio.common.annotations.processor.SkipNotAnnotatedClassWithAnnotatedParentException
import com.pushtorefresh.storio.common.annotations.processor.StorIOAnnotationsProcessor
import com.pushtorefresh.storio.common.annotations.processor.generate.Generator
import com.pushtorefresh.storio.common.annotations.processor.introspection.JavaType
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteCreator
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType
import com.pushtorefresh.storio.sqlite.annotations.processor.generate.DeleteResolverGenerator
import com.pushtorefresh.storio.sqlite.annotations.processor.generate.GetResolverGenerator
import com.pushtorefresh.storio.sqlite.annotations.processor.generate.MappingGenerator
import com.pushtorefresh.storio.sqlite.annotations.processor.generate.PutResolverGenerator
import com.pushtorefresh.storio.sqlite.annotations.processor.introspection.StorIOSQLiteColumnMeta
import com.pushtorefresh.storio.sqlite.annotations.processor.introspection.StorIOSQLiteCreatorMeta
import com.pushtorefresh.storio.sqlite.annotations.processor.introspection.StorIOSQLiteTypeMeta
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
import javax.lang.model.util.Elements
import javax.tools.Diagnostic.Kind.WARNING

/**
 * Annotation processor for StorIOSQLite.
 *
 * It'll process annotations to generate StorIOSQLite
 * Object-Mapping.
 *
 * Addition: Annotation Processor should work fast and be optimized because it's
 * part of compilation. We don't want to annoy developers, who use StorIO.
 */
// Generate file with annotation processor declaration via another Annotation Processor!
@AutoService(Processor::class)
class StorIOSQLiteProcessor : StorIOAnnotationsProcessor<StorIOSQLiteTypeMeta, StorIOSQLiteColumnMeta>() {

    override fun getSupportedAnnotationTypes() = setOf(
            StorIOSQLiteType::class.java.canonicalName,
            StorIOSQLiteColumn::class.java.canonicalName,
            StorIOSQLiteCreator::class.java.canonicalName
    )

    /**
     * Processes annotated class.
     * @param classElement type element annotated with [StorIOSQLiteType]
     *
     * @param elementUtils utils for working with elementUtils
     *
     * @return result of processing as [StorIOSQLiteTypeMeta]
     */
    override fun processAnnotatedClass(classElement: TypeElement, elementUtils: Elements): StorIOSQLiteTypeMeta {
        val storIOSQLiteType = classElement.getAnnotation(StorIOSQLiteType::class.java)

        val tableName = storIOSQLiteType.table

        if (tableName.isEmpty()) {
            throw ProcessingException(classElement, "Table name of ${classElement.simpleName} annotated with ${StorIOSQLiteType::class.java.simpleName} is empty")
        }

        val simpleName = classElement.simpleName.toString()
        val packageName = elementUtils.getPackageOf(classElement).qualifiedName.toString()

        return StorIOSQLiteTypeMeta(simpleName, packageName, storIOSQLiteType, Modifier.ABSTRACT in classElement.modifiers)
    }

    /**
     * Processes fields annotated with [StorIOSQLiteColumn].
     * @param roundEnvironment current processing environment
     *
     * @param annotatedClasses map of classes annotated with [StorIOSQLiteType]
     */
    override fun processAnnotatedFieldsOrMethods(roundEnvironment: RoundEnvironment, annotatedClasses: Map<TypeElement, StorIOSQLiteTypeMeta>) {
        val elementsAnnotatedWithStorIOSQLiteColumn = roundEnvironment.getElementsAnnotatedWith(StorIOSQLiteColumn::class.java)

        elementsAnnotatedWithStorIOSQLiteColumn.forEach { element ->
            try {
                validateAnnotatedFieldOrMethod(element)

                val columnMeta = processAnnotatedFieldOrMethod(element)

                annotatedClasses[columnMeta.enclosingElement]?.let { typeMeta ->
                    // If class already contains column with same name -> throw an exception.
                    if (columnMeta.storIOColumn.name in typeMeta.columns.keys) {
                        throw ProcessingException(element, "Column name already used in this" +
                                " class: ${columnMeta.storIOColumn.name}")
                    }

                    // If field annotation applied to both fields and methods in a same class.
                    if (typeMeta.needCreator && !columnMeta.isMethod || !typeMeta.needCreator && columnMeta.isMethod && typeMeta.columns.isNotEmpty()) {
                        throw ProcessingException(element, "Can't apply ${StorIOSQLiteColumn::class.java.simpleName} annotation to both fields and methods in a same class:" +
                                " ${typeMeta.simpleName}")
                    }

                    // If column needs creator then enclosing class needs typeMeta as well.
                    if (!typeMeta.needCreator && columnMeta.isMethod) typeMeta.needCreator = true

                    // Put meta column info.
                    typeMeta.columns += columnMeta.storIOColumn.name to columnMeta
                }
            } catch (e: SkipNotAnnotatedClassWithAnnotatedParentException) {
                messager.printMessage(WARNING, e.message)
            }
        }
    }

    /**
     * Processes annotated field and returns result of processing or throws exception.
     * @param annotatedField field that was annotated with [StorIOSQLiteColumn]
     *
     * @return non-null [StorIOSQLiteColumnMeta] with meta information about field
     */
    override fun processAnnotatedFieldOrMethod(annotatedField: Element): StorIOSQLiteColumnMeta {
        val javaType: JavaType

        try {
            javaType = JavaType.from(
                    if (annotatedField.kind == ElementKind.FIELD)
                        annotatedField.asType()
                    else
                        (annotatedField as ExecutableElement).returnType
            )
        } catch (e: Exception) {
            throw ProcessingException(annotatedField, "Unsupported type of field or method for ${StorIOSQLiteColumn::class.java.simpleName} annotation, if you" +
                    " need to serialize/deserialize field of that type -> please write your own resolver: ${e.message}")
        }

        val column = annotatedField.getAnnotation(StorIOSQLiteColumn::class.java)

        if (column.ignoreNull && annotatedField.asType().kind.isPrimitive) {
            throw ProcessingException(annotatedField, "ignoreNull should not be used for primitive type: ${annotatedField.simpleName}")
        }

        if (column.name.isEmpty()) {
            throw ProcessingException(annotatedField, "Column name is empty: ${annotatedField.simpleName}")
        }

        return StorIOSQLiteColumnMeta(annotatedField.enclosingElement, annotatedField, annotatedField.simpleName.toString(), javaType, column)
    }

    /**
     * Processes factory methods or constructors annotated with [StorIOSQLiteCreator].
     * @param roundEnvironment current processing environment
     *
     * @param annotatedClasses map of classes annotated with [StorIOSQLiteType]
     */
    override fun processAnnotatedExecutables(roundEnvironment: RoundEnvironment, annotatedClasses: Map<TypeElement, StorIOSQLiteTypeMeta>) {
        val elementsAnnotatedWithStorIOSQLiteCreator = roundEnvironment.getElementsAnnotatedWith(StorIOSQLiteCreator::class.java)

        elementsAnnotatedWithStorIOSQLiteCreator.forEach { element ->
            val executableElement = element as ExecutableElement
            validateAnnotatedExecutable(executableElement)
            val creatorMeta = StorIOSQLiteCreatorMeta(executableElement.enclosingElement, executableElement, executableElement.getAnnotation(StorIOSQLiteCreator::class.java))

            annotatedClasses[creatorMeta.enclosingElement]?.let {
                // Put meta creator info.
                // If class already contains another creator -> throw exception.
                if (it.creator == null) {
                    it.creator = executableElement
                } else {
                    throw ProcessingException(executableElement, "Only one creator method or constructor is allowed: ${executableElement.enclosingElement.simpleName}")
                }
            }
        }
    }

    override fun validateAnnotatedClassesAndColumns(annotatedClasses: Map<TypeElement, StorIOSQLiteTypeMeta>) {
        // Check that each annotated class has columns with at least one key column.
        annotatedClasses.forEach { (key, typeMeta) ->
            if (typeMeta.columns.isEmpty()) {
                throw ProcessingException(key, "Class marked with ${StorIOSQLiteType::class.java.simpleName} annotation should have at least one field or method marked with" +
                        " ${StorIOSQLiteColumn::class.java.simpleName} annotation: ${typeMeta.simpleName}")
            }

            val hasAtLeastOneKeyColumn = typeMeta.columns.values.any { it.storIOColumn.key }

            if (!hasAtLeastOneKeyColumn) {
                throw ProcessingException(key, "Class marked with ${StorIOSQLiteType::class.java.simpleName} annotation should have at least one KEY field or method marked with" +
                        " ${StorIOSQLiteColumn::class.java.simpleName} annotation: ${typeMeta.simpleName}")
            }

            if (typeMeta.needCreator && typeMeta.creator == null) {
                throw ProcessingException(key, "Class marked with ${StorIOSQLiteType::class.java.simpleName} annotation needs factory method or constructor marked with" +
                        " ${StorIOSQLiteCreator::class.java.simpleName} annotation: ${typeMeta.simpleName}")
            }

            // creator can't be null since we have checked it before
            if (typeMeta.needCreator && typeMeta.creator!!.parameters.size != typeMeta.columns.size) {
                throw ProcessingException(key, "Class marked with ${StorIOSQLiteType::class.java.simpleName} annotation needs factory method or constructor marked with" +
                        " ${StorIOSQLiteCreator::class.java.simpleName} annotation with the same amount of parameters as the number of columns: ${typeMeta.simpleName}")
            }
        }
    }

    override val typeAnnotationClass: Class<out Annotation>
        get() = StorIOSQLiteType::class.java

    override val columnAnnotationClass: Class<out Annotation>
        get() = StorIOSQLiteColumn::class.java

    override val creatorAnnotationClass: Class<out Annotation>
        get() = StorIOSQLiteCreator::class.java

    override fun createPutResolver(): Generator<StorIOSQLiteTypeMeta> = PutResolverGenerator

    override fun createGetResolver(): Generator<StorIOSQLiteTypeMeta> = GetResolverGenerator

    override fun createDeleteResolver(): Generator<StorIOSQLiteTypeMeta> = DeleteResolverGenerator

    override fun createMapping(): Generator<StorIOSQLiteTypeMeta> = MappingGenerator
}
