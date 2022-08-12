package org.treeWare.proto3.message

import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.getMetaModelResolved


fun getEncodingType(fieldMeta: EntityModel): String =
    when (val fieldType = getFieldTypeMeta(fieldMeta)) {
        FieldType.BOOLEAN -> "bool"
        FieldType.UINT8,
        FieldType.UINT16,
        FieldType.UINT32 -> "uint32"
        FieldType.UINT64 -> "uint64"
        FieldType.INT8,
        FieldType.INT16,
        FieldType.INT32 -> "int32"
        FieldType.INT64 -> "int64"
        FieldType.FLOAT -> "float"
        FieldType.DOUBLE -> "double"
        FieldType.BIG_INTEGER,
        FieldType.BIG_DECIMAL,
        FieldType.STRING,
        FieldType.UUID,
        -> "string"
        FieldType.BLOB -> "bytes"
        FieldType.TIMESTAMP -> "timestamp"
        FieldType.PASSWORD1WAY,
        FieldType.PASSWORD2WAY -> "string"
        FieldType.ASSOCIATION -> "string"
        FieldType.ENUMERATION -> snakeToCamel(getEnumerationFieldName(fieldMeta))
        FieldType.COMPOSITION -> snakeToCamel(getCompositionFieldName(fieldMeta))
        FieldType.ALIAS -> throw IllegalStateException("Unsupported field type: $fieldType")
        null -> throw java.lang.IllegalStateException("Null field type")
    }

private fun getCompositionFieldName(fieldMeta: EntityModel): String {
    val resolvedEntity = getMetaModelResolved(fieldMeta)?.compositionMeta
    return getMetaName(resolvedEntity)
}

private fun getEnumerationFieldName(fieldMeta: EntityModel): String {
    val resolvedEntity = getMetaModelResolved(fieldMeta)?.enumerationMeta
    return getMetaName(resolvedEntity)
}
