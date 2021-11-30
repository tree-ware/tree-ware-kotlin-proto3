package org.treeWare.proto3.validation

import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import org.treeWare.metaModel.traversal.AbstractLeader1Follower0MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.*
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.proto3.aux.Proto3MetaModelMapValidated
import org.treeWare.proto3.aux.getProto3MetaModelMap
import java.io.FileInputStream


fun validateProto3MetaModelMap(mainMeta: MainModel, protoDescriptorFile: String): List<String> {
    val visitor = ValidateProto3MetaModelMapVisitor(protoDescriptorFile)
    metaModelForEach(mainMeta, visitor)
    return visitor.errors
}

private class ValidateProto3MetaModelMapVisitor(
    protoDescriptorFile: String
) : AbstractLeader1Follower0MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<String>()
    private var entityPath = ""
    private val parsedProtoDescriptorMap: Map<String, Int> = parseProto(protoDescriptorFile)
    private val validatedAbsolutePaths = HashSet<String>()

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): TraversalAction {
        val aux = getProto3MetaModelMap(leaderEntityMeta1)
        entityPath = aux?.path ?: ""
        return TraversalAction.CONTINUE
    }

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        val aux = getProto3MetaModelMap(leaderFieldMeta1)
        // Check if field contains mapping to proto3 path
        if (aux?.path != null) {
            val absolutePath = if (aux.path.contains(".proto:/")) aux.path else "$entityPath/${aux.path}"
            if (validatedAbsolutePaths.contains(absolutePath)) {
                val fullName = leaderFieldMeta1.getAux<Resolved>(RESOLVED_AUX)?.fullName
                errors.add("$fullName has duplicate mapping $absolutePath")
            } else if (parsedProtoDescriptorMap.containsKey(absolutePath)) {
                validatedAbsolutePaths.add(absolutePath)
                val protoFieldNumber = parsedProtoDescriptorMap[absolutePath]
                aux.validated = protoFieldNumber?.let { Proto3MetaModelMapValidated(absolutePath, it) }
            } else {
                val fullName = leaderFieldMeta1.getAux<Resolved>(RESOLVED_AUX)?.fullName
                errors.add("$fullName mapping $absolutePath does not exist")
            }
        }
        return TraversalAction.CONTINUE
    }

    private fun parseProto(protoDescriptorFile: String): Map<String, Int> {
        if (protoDescriptorFile.isEmpty()) return emptyMap()
        val protoDescriptorMap = HashMap<String, Int>()
        val fileDescriptorSet = FileDescriptorSet.parseFrom(FileInputStream(protoDescriptorFile))
        for (protoFile in fileDescriptorSet.fileList) {
            for (protoMessage in protoFile.messageTypeList) {
                for (protoMessageField in protoMessage.fieldList) {
                    val path = "${protoFile.name}:/${protoMessage.name}/${protoMessageField.name}"
                    protoDescriptorMap.putIfAbsent(path, protoMessageField.number)
                }
            }
        }
        return protoDescriptorMap
    }
}