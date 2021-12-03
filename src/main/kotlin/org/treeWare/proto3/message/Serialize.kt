package org.treeWare.proto3.message

import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat
import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.proto3.aux.getProto3MessageInfo
import org.treeWare.proto3.aux.getProto3MetaModelMap

internal fun serialize(mainModel: MainModel, output: CodedOutputStream) {
    forEach(mainModel, SerializeVisitor(output))
}

private class SerializeVisitor(
    private val output: CodedOutputStream
) : AbstractLeader1Follower0ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    private fun writeTag(fieldNumber: Int, wireType: Int) {
        val tag = (fieldNumber shl WIRE_TYPE_BITS) or wireType
        output.writeUInt32NoTag(tag)
    }

    private fun writeLength(length: Int) {
        output.writeUInt32NoTag(length)
    }

    // Leader1Follower0ModelVisitor methods

    override fun visit(leaderEntity1: EntityModel): TraversalAction {
        // Entities are represented as messages, and they are not packed, so include tags.
        val length = getProto3MessageInfo(leaderEntity1)?.serializedSize ?: return TraversalAction.CONTINUE
        // NOTE: even if a message is empty, its tag and length (0) need to be included.
        val parentFieldMeta = leaderEntity1.parent.meta
        val fieldNumber = getProto3MetaModelMap(parentFieldMeta)?.validated?.fieldNumber
            ?: throw IllegalStateException()
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

    override fun visit(leaderField1: SingleFieldModel): TraversalAction {
        val length = getProto3MessageInfo(leaderField1)?.serializedSize ?: return TraversalAction.CONTINUE
        if (length <= 0) return TraversalAction.CONTINUE
        if (isPackedType(leaderField1)) {
            val fieldNumber = getProto3MetaModelMap(leaderField1.meta)?.validated?.fieldNumber
                ?: throw IllegalStateException()
            val wireType = getWireType(leaderField1)
            writeTag(fieldNumber, wireType)
        }
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderField1: ListFieldModel): TraversalAction {
        val length = getProto3MessageInfo(leaderField1)?.serializedSize ?: return TraversalAction.CONTINUE
        if (length <= 0) return TraversalAction.CONTINUE
        if (isPackedType(leaderField1)) {
            val fieldNumber = getProto3MetaModelMap(leaderField1.meta)?.validated?.fieldNumber
                ?: throw IllegalStateException()
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

    override fun visit(leaderValue1: PrimitiveModel): TraversalAction {
        val parentMeta = leaderValue1.parent.meta ?: return TraversalAction.CONTINUE
        val value = leaderValue1.value ?: TraversalAction.CONTINUE
        when (val fieldType = getFieldTypeMeta(parentMeta)) {
            FieldType.BOOLEAN -> {
                val boolean = value as Boolean
                if (boolean) output.writeBoolNoTag(boolean)
            }
            FieldType.BYTE,
            FieldType.SHORT,
            FieldType.INT -> {
                val int = value as Int
                if (int != 0) output.writeInt32NoTag(int)
            }
            FieldType.LONG -> {
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
            FieldType.STRING,
            FieldType.UUID -> {
                val string = value as String
                if (string.isNotEmpty()) {
                    // Non-packable type, so include tag.
                    val fieldNumber = getProto3MetaModelMap(parentMeta)?.validated?.fieldNumber
                        ?: throw IllegalStateException()
                    output.writeString(fieldNumber, string)
                }
            }
            FieldType.BLOB -> {
                val bytes = value as ByteArray
                if (bytes.isNotEmpty()) {
                    // Non-packable type, so include tag.
                    val fieldNumber = getProto3MetaModelMap(parentMeta)?.validated?.fieldNumber
                        ?: throw IllegalStateException()
                    output.writeByteArray(fieldNumber, bytes)
                }
            }
            FieldType.TIMESTAMP -> {
                val long = value as Long
                if (long != 0L) output.writeUInt64NoTag(long)
            }
            else -> throw IllegalStateException("Invalid primitive field type: $fieldType")
        }
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderValue1: EnumerationModel): TraversalAction {
        // Enumeration values are packed, so don't include tag.
        val enumNumber =
            getProto3MetaModelMap(leaderValue1.meta)?.validated?.fieldNumber ?: return TraversalAction.CONTINUE
        output.writeEnumNoTag(enumNumber)
        return TraversalAction.CONTINUE
    }
}