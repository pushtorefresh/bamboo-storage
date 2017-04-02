package com.pushtorefresh.storio.common.annotations.processor.introspection

import javax.lang.model.element.ExecutableElement

open class StorIOTypeMeta<out TypeAnnotation : Annotation, ColumnMeta : StorIOColumnMeta<*>>
@JvmOverloads constructor(
        val simpleName: String,
        val packageName: String,
        val storIOType: TypeAnnotation,
        var needCreator: Boolean = false) {

    var creator: ExecutableElement? = null

    // Yep, this is MODIFIABLE Map, please use it carefully.
    val columns: MutableMap<String, ColumnMeta> = mutableMapOf()

    val orderedColumns: Collection<ColumnMeta>
        get() = when {
            needCreator -> {
                val params = mutableListOf<String>()
                creator?.let {
                    it.parameters.mapTo(params) { it.simpleName.toString() }
                }
                val orderedColumns = mutableListOf<ColumnMeta?>().apply {
                    (0..columns.size - 1).forEach { add(null) }
                }
                columns.values.forEach { orderedColumns[params.indexOf(it.realElementName)] = it }
                orderedColumns.map { it as ColumnMeta }
            }
            else -> columns.values
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as StorIOTypeMeta<*, *>

        if (simpleName != other.simpleName) return false
        if (packageName != other.packageName) return false
        if (storIOType != other.storIOType) return false
        if (columns != other.columns) return false

        return true
    }

    override fun hashCode(): Int {
        var result = simpleName.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + storIOType.hashCode()
        result = 31 * result + columns.hashCode()
        return result
    }

    override fun toString() = "StorIOTypeMeta(simpleName='$simpleName', packageName='$packageName', storIOType=$storIOType, needCreator=$needCreator, creator=$creator, columns=$columns)"
}