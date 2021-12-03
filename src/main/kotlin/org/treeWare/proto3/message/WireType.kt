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
    FieldType.BYTE,
    FieldType.SHORT,
    FieldType.INT,
    FieldType.LONG -> WireFormat.WIRETYPE_VARINT
    FieldType.FLOAT -> WireFormat.WIRETYPE_FIXED32
    FieldType.DOUBLE -> WireFormat.WIRETYPE_FIXED64
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
    FieldType.BYTE,
    FieldType.SHORT,
    FieldType.INT,
    FieldType.LONG,
    FieldType.FLOAT,
    FieldType.DOUBLE -> true
    FieldType.STRING,
    FieldType.UUID,
    FieldType.BLOB -> false
    FieldType.TIMESTAMP,
    FieldType.ENUMERATION -> true
    FieldType.COMPOSITION -> false
    else -> throw IllegalStateException("Illegal field type: $fieldType")
}