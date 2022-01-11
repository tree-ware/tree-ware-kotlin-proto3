package org.treeWare.proto3.message

import com.google.protobuf.WireFormat
import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.model.core.FieldModel

// com.google.protobuf.WireFormat.makeTag() and com.google.protobuf.WireFormat.TAG_TYPE_BITS
// are not public. So the number of bits for the wire-type has to be re-defined here.
const val WIRE_TYPE_BITS = 3

fun getWireType(fieldModel: FieldModel): Int = when (val fieldType = getFieldTypeMeta(fieldModel.meta)) {
    FieldType.BOOLEAN,
    FieldType.UINT8,
    FieldType.UINT16,
    FieldType.UINT32,
    FieldType.UINT64,
    FieldType.INT8,
    FieldType.INT16,
    FieldType.INT32,
    FieldType.INT64 -> WireFormat.WIRETYPE_VARINT
    FieldType.FLOAT -> WireFormat.WIRETYPE_FIXED32
    FieldType.DOUBLE -> WireFormat.WIRETYPE_FIXED64
    FieldType.BIG_INTEGER,
    FieldType.BIG_DECIMAL,
    FieldType.STRING,
    FieldType.UUID,
    FieldType.BLOB -> WireFormat.WIRETYPE_LENGTH_DELIMITED
    FieldType.TIMESTAMP,
    FieldType.ENUMERATION -> WireFormat.WIRETYPE_VARINT
    FieldType.COMPOSITION -> WireFormat.WIRETYPE_LENGTH_DELIMITED
    else -> throw IllegalStateException("Illegal field type: $fieldType")
}

fun isPackedType(fieldModel: FieldModel): Boolean = when (val fieldType = getFieldTypeMeta(fieldModel.meta)) {
    FieldType.BOOLEAN,
    FieldType.UINT8,
    FieldType.UINT16,
    FieldType.UINT32,
    FieldType.UINT64,
    FieldType.INT8,
    FieldType.INT16,
    FieldType.INT32,
    FieldType.INT64,
    FieldType.FLOAT,
    FieldType.DOUBLE -> true
    FieldType.BIG_INTEGER,
    FieldType.BIG_DECIMAL -> false
    FieldType.TIMESTAMP -> true
    FieldType.STRING,
    FieldType.UUID,
    FieldType.BLOB -> false
    FieldType.ENUMERATION -> true
    FieldType.COMPOSITION -> false
    else -> throw IllegalStateException("Illegal field type: $fieldType")
}