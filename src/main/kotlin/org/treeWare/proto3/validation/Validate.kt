package org.treeWare.proto3.validation

import org.treeWare.metaModel.traversal.AbstractLeader1Follower0MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.proto3.aux.Proto3MetaModelMapValidated
import org.treeWare.proto3.aux.getProto3MetaModelMap


fun validate(mainMeta: MainModel): List<String> {
    val visitor = ValidationVisitor()
    metaModelForEach(mainMeta, visitor)
    return visitor.errors
}

private class ValidationVisitor : AbstractLeader1Follower0MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<String>()
    private var entityPath = ""

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
            aux.validated = Proto3MetaModelMapValidated(absolutePath)
        }
        return TraversalAction.CONTINUE
    }
}
