package org.treeWare.proto3.validation

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.metaModel.getMetaNumber
import org.treeWare.metaModel.traversal.AbstractLeader1MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.getMetaModelResolved
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.proto3.aux.Proto3MetaModelMapValidated
import org.treeWare.proto3.aux.getProto3MetaModelMap
import java.io.FileInputStream

fun validateProto3MetaModelMap(metaModel: MutableEntityModel, protoDescriptorFile: String): List<String> {
    val visitor = ValidateProto3MetaModelMapVisitor(protoDescriptorFile)
    metaModelForEach(metaModel, visitor)
    return visitor.errors
}

private enum class ChildType { ENUMERATION_VALUE, FIELD }

private class ValidateProto3MetaModelMapVisitor(
    protoDescriptorFile: String
) : AbstractLeader1MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<String>()
    private var parentPath = ""
    private val parsedProtoDescriptorMap: Map<String, Int> = parseProto(protoDescriptorFile)
    private val validatedAbsolutePaths = HashSet<String>()

    override fun visitEnumerationMeta(leaderEnumerationMeta1: EntityModel): TraversalAction =
        visitParentMeta(leaderEnumerationMeta1)

    override fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel): TraversalAction =
        visitChildMeta(leaderEnumerationValueMeta1, ChildType.ENUMERATION_VALUE)

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): TraversalAction =
        visitParentMeta(leaderEntityMeta1)

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction =
        visitChildMeta(leaderFieldMeta1, ChildType.FIELD)

    // Helpers

    private fun visitParentMeta(parentMeta: EntityModel): TraversalAction {
        val aux = getProto3MetaModelMap(parentMeta) ?: return TraversalAction.ABORT_SUB_TREE
        parentPath = aux.path ?: ""
        return TraversalAction.CONTINUE
    }

    private fun visitChildMeta(childMeta: EntityModel, childType: ChildType): TraversalAction {
        val resolved = getMetaModelResolved(childMeta) ?: throw IllegalStateException("Meta-model not resolved")
        val fullName = resolved.fullName
        val aux = getProto3MetaModelMap(childMeta)
        if (aux == null) {
            if (childType == ChildType.ENUMERATION_VALUE) errors.add("$fullName is not mapped but parent is mapped")
            return TraversalAction.ABORT_SUB_TREE
        }
        val metaNumber = getMetaNumber(childMeta)?.toInt()
            ?: throw IllegalStateException("Meta-model number is missing")
        if (aux.path != null) {
            // Field is mapped to an existing proto.
            val absolutePath = if (aux.path.contains(".proto:/")) aux.path else "$parentPath/${aux.path}"
            if (validatedAbsolutePaths.contains(absolutePath)) {
                errors.add("$fullName has duplicate mapping $absolutePath")
            } else if (!parsedProtoDescriptorMap.containsKey(absolutePath)) {
                errors.add("$fullName mapping $absolutePath does not exist")
            } else {
                val protoFieldNumber = parsedProtoDescriptorMap[absolutePath]
                if (protoFieldNumber != metaNumber) {
                    errors.add("$fullName number $metaNumber does not match $absolutePath number $protoFieldNumber")
                } else {
                    validatedAbsolutePaths.add(absolutePath)
                    aux.validated = Proto3MetaModelMapValidated(absolutePath, metaNumber)
                }
            }
        } else {
            // Field is mapped to a generated proto.
            aux.validated = Proto3MetaModelMapValidated(null, metaNumber)
        }
        if (childType == ChildType.FIELD) {
            when (getFieldTypeMeta(childMeta)) {
                FieldType.ENUMERATION -> {
                    val enumerationAux = getProto3MetaModelMap(resolved.enumerationMeta)
                    if (enumerationAux == null) {
                        errors.add("$fullName enumeration field is mapped but enumeration is not mapped")
                    }
                }
                FieldType.COMPOSITION -> {
                    val entityAux = getProto3MetaModelMap(resolved.compositionMeta)
                    if (entityAux == null) {
                        errors.add("$fullName composition field is mapped but entity is not mapped")
                        return TraversalAction.ABORT_SUB_TREE
                    }
                }
                else -> {}
            }
        }
        return TraversalAction.CONTINUE
    }

    private fun parseProto(protoDescriptorFile: String): Map<String, Int> {
        if (protoDescriptorFile.isEmpty()) return emptyMap()
        val protoDescriptorMap = HashMap<String, Int>()
        val fileDescriptorSet = FileDescriptorSet.parseFrom(FileInputStream(protoDescriptorFile))
        for (protoFile in fileDescriptorSet.fileList) {
            collectProtoEnums(protoDescriptorMap, "${protoFile.name}:", protoFile.enumTypeList)
            for (protoMessage in protoFile.messageTypeList) {
                collectProtoEnums(
                    protoDescriptorMap,
                    "${protoFile.name}:/${protoMessage.name}",
                    protoMessage.enumTypeList
                )
                for (protoMessageField in protoMessage.fieldList) {
                    val path = "${protoFile.name}:/${protoMessage.name}/${protoMessageField.name}"
                    protoDescriptorMap.putIfAbsent(path, protoMessageField.number)
                }
            }
        }
        return protoDescriptorMap
    }

    private fun collectProtoEnums(
        protoDescriptorMap: HashMap<String, Int>,
        pathPrefix: String,
        enumTypeList: List<DescriptorProtos.EnumDescriptorProto>
    ) {
        for (protoEnum in enumTypeList) {
            for (protoEnumValue in protoEnum.valueList) {
                val path = "$pathPrefix/${protoEnum.name}/${protoEnumValue.name}"
                protoDescriptorMap.putIfAbsent(path, protoEnumValue.number)
            }
        }
    }
}