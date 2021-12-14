package org.treeWare.proto3.validation

import org.treeWare.metaModel.newProto3AddressBookMetaModel
import org.treeWare.metaModel.traversal.AbstractLeader1MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.*
import org.treeWare.model.readFile
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.proto3.aux.getProto3MetaModelMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails


class ValidateProto3MetaModelMapTests {
    @Test
    fun `Proto3 mapping validation must fail for invalid paths`() {
        // TODO(Deepak-Nulu) verify errors
        assertFails { newProto3AddressBookMetaModel(null, null, "") }
    }

    @Test
    fun `Proto3 mapping validation must pass for valid paths`() {
        val metaModel = newProto3AddressBookMetaModel(
            null, null, "build/generated/source/proto/test/descriptor_set.desc"
        )

        val proto3Mappings = getProto3Mappings(metaModel)
        val expected = readFile("validation/address_book_absolute_paths.txt")
        val actual = proto3Mappings.joinToString("\n")
        assertEquals(expected, actual)
    }
}

private fun getProto3Mappings(mainMeta: MainModel): List<String> {
    val visitor = GetProto3MappingsVisitor()
    metaModelForEach(mainMeta, visitor)
    return visitor.mappings
}

private class GetProto3MappingsVisitor :
    AbstractLeader1MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val mappings = mutableListOf<String>()

    override fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel): TraversalAction {
        val aux = getProto3MetaModelMap(leaderEnumerationValueMeta1)
        val fullName = leaderEnumerationValueMeta1.getAux<Resolved>(RESOLVED_AUX)?.fullName
        aux?.validated?.also { mappings.add("$fullName -> ${it.path} = ${it.fieldNumber}") }
        return TraversalAction.CONTINUE
    }

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        val aux = getProto3MetaModelMap(leaderFieldMeta1)
        val fullName = leaderFieldMeta1.getAux<Resolved>(RESOLVED_AUX)?.fullName
        aux?.validated?.also { mappings.add("$fullName -> ${it.path} = ${it.fieldNumber}") }
        return TraversalAction.CONTINUE
    }
}