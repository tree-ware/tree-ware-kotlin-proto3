package org.treeWare.proto3.message

import com.google.protobuf.CodedOutputStream
import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.proto3.aux.Proto3MessageInfo
import org.treeWare.proto3.aux.getProto3MetaModelMap
import org.treeWare.proto3.aux.setProto3MessageInfo

internal fun computeSerializedSize(mainModel: MainModel) {
    forEach(mainModel, ComputeSerializedSizeVisitor(), true)
}

private class ComputeSerializedSizeVisitor :
    AbstractLeader1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    private val sizeStack = ArrayDeque<Int>()

    private fun visitElement(tagSize: Int = 0): TraversalAction {
        addToParentSize(tagSize)
        sizeStack.addLast(0)
        return TraversalAction.CONTINUE
    }

    private fun leaveElement(element: ElementModel, includeChildLengthInParent: Boolean = false) {
        val size = sizeStack.removeLast()
        val aux = Proto3MessageInfo(size)
        setProto3MessageInfo(element, aux)
        val lengthSize = if (includeChildLengthInParent) CodedOutputStream.computeUInt32SizeNoTag(size) else 0
        addToParentSize(lengthSize + size)
    }

    private fun addToParentSize(childSize: Int) {
        if (childSize <= 0) return
        sizeStack.removeLastOrNull()?.let {
            sizeStack.addLast(it + childSize)
        }
    }

    // Leader1ModelVisitor methods

    override fun visitMain(leaderMain1: MainModel): TraversalAction = visitElement()
    override fun leaveMain(leaderMain1: MainModel) = leaveElement(leaderMain1)

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction {
        if (isRootEntity(leaderEntity1)) return visitElement()
        else {
            // Entities are represented as messages, and they are not packed, so include tag size.
            val parentFieldMeta = leaderEntity1.parent.meta
            val fieldNumber = getProto3MetaModelMap(parentFieldMeta)?.validated?.fieldNumber
                ?: return TraversalAction.CONTINUE
            val tagSize = CodedOutputStream.computeTagSize(fieldNumber)
            return visitElement(tagSize)
        }
    }

    override fun leaveEntity(leaderEntity1: EntityModel) = leaveElement(leaderEntity1, !isRootEntity(leaderEntity1))

    // Fields

    // NOTE: repeated elements that are packable share a single tag. Field-
    // models include the tag size for such value-models. Field-models do not
    // include the tag size for non-packable value-models because the value-
    // models need to include their tag each time they are repeated. See NOTE
    // further below for more details.

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction {
        val tagSize = if (isPackedType(leaderField1)) {
            val fieldNumber = getProto3MetaModelMap(leaderField1.meta)?.validated?.fieldNumber
                ?: return TraversalAction.CONTINUE
            CodedOutputStream.computeTagSize(fieldNumber)
        } else 0
        return visitElement(tagSize)
    }

    override fun leaveSingleField(leaderField1: SingleFieldModel) = leaveElement(leaderField1)

    override fun visitListField(leaderField1: ListFieldModel): TraversalAction {
        val tagSize =
            if (leaderField1.values.isEmpty()) 0
            else if (isPackedType(leaderField1)) {
                val fieldNumber = getProto3MetaModelMap(leaderField1.meta)?.validated?.fieldNumber
                    ?: return TraversalAction.CONTINUE
                CodedOutputStream.computeTagSize(fieldNumber)
            } else 0
        return visitElement(tagSize)
    }

    override fun leaveListField(leaderField1: ListFieldModel) =
        leaveElement(leaderField1, leaderField1.values.isNotEmpty() && isPackedType(leaderField1))

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
        val size: Int = when (val fieldType = getFieldTypeMeta(parentMeta)) {
            FieldType.BOOLEAN -> {
                val boolean = value as Boolean
                if (!boolean) 0 else CodedOutputStream.computeBoolSizeNoTag(true)
            }
            FieldType.UINT8,
            FieldType.UINT16,
            FieldType.UINT32 -> {
                val int = (value as UInt).toInt()
                if (int == 0) 0 else CodedOutputStream.computeUInt32SizeNoTag(int)
            }
            FieldType.UINT64 -> {
                val long = (value as ULong).toLong()
                if (long == 0L) 0 else CodedOutputStream.computeUInt64SizeNoTag(long)
            }
            FieldType.INT8,
            FieldType.INT16,
            FieldType.INT32 -> {
                val int = value as Int
                if (int == 0) 0 else CodedOutputStream.computeInt32SizeNoTag(int)
            }
            FieldType.INT64 -> {
                val long = value as Long
                if (long == 0L) 0 else CodedOutputStream.computeInt64SizeNoTag(long)
            }
            FieldType.FLOAT -> {
                val float = value as Float
                if (float == 0.0F) 0 else CodedOutputStream.computeFloatSizeNoTag(float)
            }
            FieldType.DOUBLE -> {
                val double = value as Double
                if (double == 0.0) 0 else CodedOutputStream.computeDoubleSizeNoTag(double)
            }
            FieldType.BIG_INTEGER,
            FieldType.BIG_DECIMAL -> {
                val string = value.toString()
                if (string.isEmpty()) 0 else {
                    // Non-packable type, so include tag size.
                    val fieldNumber = getProto3MetaModelMap(parentMeta)?.validated?.fieldNumber
                        ?: return TraversalAction.CONTINUE
                    val tagSize = CodedOutputStream.computeTagSize(fieldNumber)
                    tagSize + CodedOutputStream.computeStringSizeNoTag(string)
                }
            }
            FieldType.TIMESTAMP -> {
                val long = value as Long
                if (long == 0L) 0 else CodedOutputStream.computeUInt64SizeNoTag(long)
            }
            FieldType.STRING,
            FieldType.UUID -> {
                val string = value as String
                if (string.isEmpty()) 0 else {
                    // Non-packable type, so include tag size.
                    val fieldNumber = getProto3MetaModelMap(parentMeta)?.validated?.fieldNumber
                        ?: return TraversalAction.CONTINUE
                    val tagSize = CodedOutputStream.computeTagSize(fieldNumber)
                    tagSize + CodedOutputStream.computeStringSizeNoTag(string)
                }
            }
            FieldType.BLOB -> {
                val bytes = value as ByteArray
                if (bytes.isEmpty()) 0 else {
                    // Non-packable type, so include tag size.
                    val fieldNumber = getProto3MetaModelMap(parentMeta)?.validated?.fieldNumber
                        ?: return TraversalAction.CONTINUE
                    val tagSize = CodedOutputStream.computeTagSize(fieldNumber)
                    tagSize + CodedOutputStream.computeByteArraySizeNoTag(bytes)
                }
            }
            else -> throw IllegalStateException("Invalid primitive field type: $fieldType")
        }
        addToParentSize(size)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValue1: EnumerationModel): TraversalAction {
        // Enumeration values are packed, so don't include tag size.
        val enumNumber =
            getProto3MetaModelMap(leaderValue1.meta)?.validated?.fieldNumber ?: return TraversalAction.CONTINUE
        val size = CodedOutputStream.computeEnumSizeNoTag(enumNumber)
        addToParentSize(size)
        return TraversalAction.CONTINUE
    }
}