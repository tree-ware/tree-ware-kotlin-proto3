package org.treeWare.proto3.message

import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat
import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.proto3.aux.getProto3MessageInfo
import org.treeWare.proto3.aux.getProto3MetaModelMap

internal fun serialize(mainModel: MainModel, output: CodedOutputStream) {
    forEach(mainModel, SerializeVisitor(output), true)
}

private class SerializeVisitor(
    private val output: CodedOutputStream
) : AbstractLeader1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    private var isSkipLeave = false

    private fun writeTag(fieldNumber: Int, wireType: Int) {
        val tag = (fieldNumber shl WIRE_TYPE_BITS) or wireType
        output.writeUInt32NoTag(tag)
    }

    private fun writeLength(length: Int) {
        output.writeUInt32NoTag(length)
    }

    // Leader1ModelVisitor methods

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction {
        // Entities are represented as messages, and they are not packed, so include tags.
        val length = getProto3MessageInfo(leaderEntity1)?.serializedSize ?: return TraversalAction.ABORT_SUB_TREE
        // NOTE: even if a message is empty, its tag and length (0) need to be included.
        val parentFieldMeta = leaderEntity1.parent.meta
        val fieldNumber = getProto3MetaModelMap(parentFieldMeta)?.validated?.fieldNumber
            ?: return if (isRootEntity(leaderEntity1)) TraversalAction.CONTINUE
            else TraversalAction.ABORT_SUB_TREE.also { isSkipLeave = true }
        writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        writeLength(length)
        return TraversalAction.CONTINUE
    }

    // Fields

    // NOTE: repeated elements that are packable share a single tag. Field-
    // models include the tags for such value-models. Field-models do not
    // include the tags for non-packable value-models because the value-
    // models need to include their tags each time they are repeated. See NOTE
    // further below for more details.

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction {
        val length = getProto3MessageInfo(leaderField1)?.serializedSize ?: return TraversalAction.ABORT_SUB_TREE
        if (length <= 0) return TraversalAction.ABORT_SUB_TREE
        if (isPackedType(leaderField1)) {
            val fieldNumber = getProto3MetaModelMap(leaderField1.meta)?.validated?.fieldNumber
                ?: return TraversalAction.ABORT_SUB_TREE.also { isSkipLeave = true }
            val wireType = getWireType(leaderField1)
            writeTag(fieldNumber, wireType)
        }
        return TraversalAction.CONTINUE
    }

    override fun visitListField(leaderField1: ListFieldModel): TraversalAction {
        val length = getProto3MessageInfo(leaderField1)?.serializedSize ?: return TraversalAction.ABORT_SUB_TREE
        if (length <= 0) return TraversalAction.ABORT_SUB_TREE
        if (isPackedType(leaderField1)) {
            val fieldNumber = getProto3MetaModelMap(leaderField1.meta)?.validated?.fieldNumber
                ?: return TraversalAction.ABORT_SUB_TREE.also { isSkipLeave = true }
            writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED)
            writeLength(length)
        }
        return TraversalAction.CONTINUE
    }

    // SetFieldModel overrides are not needed because currently set-fields
    // support only entities, and repeated messages are not packed. So there
    // is no contribution from the set-fields themselves.

    // Values

    // NOTE: repeated elements that are packable share a single tag. To avoid
    // having to determine if an element is repeated (ListFieldModel) or not
    // (SingleFieldModel), the values types below that are packable do not
    // include their tag sizes; their tag sizes get included in their parents
    // (field-models). Repeated elements that are not packable must include
    // their tags every time they are repeated. So the value types below that
    // are not packable always include their tag sizes; their parents (field-
    // models) do not include their tag sizes.

    override fun visitPrimitive(leaderValue1: PrimitiveModel): TraversalAction {
        val parentMeta = leaderValue1.parent.meta ?: return TraversalAction.CONTINUE
        val value = leaderValue1.value
        when (val fieldType = getFieldTypeMeta(parentMeta)) {
            FieldType.BOOLEAN -> {
                val boolean = value as Boolean
                if (boolean) output.writeBoolNoTag(true)
            }
            FieldType.UINT8,
            FieldType.UINT16,
            FieldType.UINT32 -> {
                val int = (value as UInt).toInt()
                if (int != 0) output.writeUInt32NoTag(int)
            }
            FieldType.UINT64 -> {
                val long = (value as ULong).toLong()
                if (long != 0L) output.writeUInt64NoTag(long)
            }
            FieldType.INT8,
            FieldType.INT16,
            FieldType.INT32 -> {
                val int = value as Int
                if (int != 0) output.writeInt32NoTag(int)
            }
            FieldType.INT64 -> {
                val long = value as Long
                if (long != 0L) output.writeInt64NoTag(long)
            }
            FieldType.FLOAT -> {
                val float = value as Float
                if (float != 0.0F) output.writeFloatNoTag(float)
            }
            FieldType.DOUBLE -> {
                val double = value as Double
                if (double != 0.0) output.writeDoubleNoTag(double)
            }
            FieldType.BIG_INTEGER,
            FieldType.BIG_DECIMAL -> {
                val string = value.toString()
                if (string.isNotEmpty()) {
                    // Non-packable type, so include tag.
                    val fieldNumber = getProto3MetaModelMap(parentMeta)?.validated?.fieldNumber
                        ?: return TraversalAction.CONTINUE
                    output.writeString(fieldNumber, string)
                }
            }
            FieldType.TIMESTAMP -> {
                val long = value as Long
                if (long != 0L) output.writeUInt64NoTag(long)
            }
            FieldType.STRING,
            FieldType.UUID -> {
                val string = value as String
                if (string.isNotEmpty()) {
                    // Non-packable type, so include tag.
                    val fieldNumber = getProto3MetaModelMap(parentMeta)?.validated?.fieldNumber
                        ?: return TraversalAction.CONTINUE
                    output.writeString(fieldNumber, string)
                }
            }
            FieldType.BLOB -> {
                val bytes = value as ByteArray
                if (bytes.isNotEmpty()) {
                    // Non-packable type, so include tag.
                    val fieldNumber = getProto3MetaModelMap(parentMeta)?.validated?.fieldNumber
                        ?: return TraversalAction.CONTINUE
                    output.writeByteArray(fieldNumber, bytes)
                }
            }
            else -> throw IllegalStateException("Invalid primitive field type: $fieldType")
        }
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValue1: EnumerationModel): TraversalAction {
        // Enumeration values are packed, so don't include tag.
        val enumNumber =
            getProto3MetaModelMap(leaderValue1.meta)?.validated?.fieldNumber ?: return TraversalAction.CONTINUE
        output.writeEnumNoTag(enumNumber)
        return TraversalAction.CONTINUE
    }
}