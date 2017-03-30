package com.pushtorefresh.storio.contentresolver.annotations.processor.generate;

import com.pushtorefresh.storio.common.annotations.processor.generate.Generator;
import com.pushtorefresh.storio.contentresolver.annotations.processor.introspection.StorIOContentResolverTypeMeta;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.pushtorefresh.storio.common.annotations.processor.generate.Common.INSTANCE;
import static javax.lang.model.element.Modifier.PUBLIC;

public class DeleteResolverGenerator implements Generator<StorIOContentResolverTypeMeta> {

    private static final String SUFFIX = "StorIOContentResolverDeleteResolver";

    @NotNull
    public static String generateName(@NotNull StorIOContentResolverTypeMeta storIOSQLiteTypeMeta) {
        return storIOSQLiteTypeMeta.getSimpleName() + SUFFIX;
    }

    @NotNull
    public JavaFile generateJavaFile(@NotNull final StorIOContentResolverTypeMeta storIOContentResolverTypeMeta) {
        final ClassName storIOContentResolverTypeClassName = ClassName.get(
            storIOContentResolverTypeMeta.getPackageName(),
            storIOContentResolverTypeMeta.getSimpleName());

        final TypeSpec deleteResolver = TypeSpec.classBuilder(generateName(storIOContentResolverTypeMeta))
                .addJavadoc("Generated resolver for Delete Operation\n")
                .addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get("com.pushtorefresh.storio.contentresolver.operations.delete", "DefaultDeleteResolver"), storIOContentResolverTypeClassName))
                .addMethod(createMapToDeleteQueryMethodSpec(storIOContentResolverTypeMeta, storIOContentResolverTypeClassName))
                .build();

        return JavaFile
                .builder(storIOContentResolverTypeMeta.getPackageName(), deleteResolver)
                .indent(INSTANCE.getINDENT())
                .build();
    }

    @NotNull
    private MethodSpec createMapToDeleteQueryMethodSpec(@NotNull final StorIOContentResolverTypeMeta storIOContentResolverTypeMeta, @NotNull final ClassName storIOContentResolverTypeClassName) {
        final Map<String, String> where = QueryGenerator.createWhere(storIOContentResolverTypeMeta, "object");

        String deleteUri = storIOContentResolverTypeMeta.getStorIOType().deleteUri();
        if (deleteUri == null || deleteUri.length() == 0) {
            deleteUri = storIOContentResolverTypeMeta.getStorIOType().uri();
        }

        return MethodSpec.methodBuilder("mapToDeleteQuery")
                .addJavadoc("{@inheritDoc}\n")
                .addAnnotation(Override.class)
                .addAnnotation(INSTANCE.getANDROID_NON_NULL_ANNOTATION_CLASS_NAME())
                .addModifiers(PUBLIC)
                .returns(ClassName.get("com.pushtorefresh.storio.contentresolver.queries", "DeleteQuery"))
                .addParameter(ParameterSpec.builder(storIOContentResolverTypeClassName, "object")
                        .addAnnotation(INSTANCE.getANDROID_NON_NULL_ANNOTATION_CLASS_NAME())
                        .build())
                .addCode("return DeleteQuery.builder()\n" +
                        INSTANCE.getINDENT() + ".uri($S)\n" +
                        INSTANCE.getINDENT() + ".where($S)\n" +
                        INSTANCE.getINDENT() + ".whereArgs($L)\n" +
                        INSTANCE.getINDENT() + ".build();\n",
                        deleteUri,
                        where.get(QueryGenerator.WHERE_CLAUSE),
                        where.get(QueryGenerator.WHERE_ARGS))
                .build();
    }
}
