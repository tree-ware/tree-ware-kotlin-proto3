package org.treeWare.proto3.validation

import org.treeWare.metaModel.*
import org.treeWare.metaModel.traversal.AbstractLeader1MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.defaultRootEntityFactory
import org.treeWare.model.core.getMetaModelResolved
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.proto3.aux.Proto3MetaModelMapAuxPlugin
import org.treeWare.proto3.aux.getProto3MetaModelMap
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ValidateProto3MetaModelMapTests {
    @Test
    fun `Proto3 mapping validation must fail for invalid enumeration mapping`() {
        val testPackageJson = """
            |{
            |  "name": "test.main",
            |  "enumerations": [
            |    {
            |      "proto3_": {
            |        "path": "address_book.proto:/Invalid/Path"
            |      },
            |      "name": "address_book_color",
            |      "values": [
            |        {
            |          "proto3_": {
            |            "path": "WHITE"
            |          },
            |          "name": "white",
            |          "number": 0
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newProto3TestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("/test.main/address_book_color/white mapping address_book.proto:/Invalid/Path/WHITE does not exist")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Proto3 mapping validation must fail for invalid enumeration value mapping`() {
        val testPackageJson = """
            |{
            |  "name": "test.main",
            |  "enumerations": [
            |    {
            |      "proto3_": {
            |        "path": "person.proto:/Relationship"
            |      },
            |      "name": "address_book_relationship",
            |      "values": [
            |        {
            |          "proto3_": {
            |            "path": "INVALID_ENUM_VALUE"
            |          },
            |          "name": "unknown",
            |          "number": 0
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newProto3TestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("/test.main/address_book_relationship/unknown mapping person.proto:/Relationship/INVALID_ENUM_VALUE does not exist")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Proto3 mapping validation must fail for invalid enumeration number`() {
        val testPackageJson = """
            |{
            |  "name": "test.main",
            |  "enumerations": [
            |    {
            |      "proto3_": {
            |        "path": "person.proto:/Relationship"
            |      },
            |      "name": "address_book_relationship",
            |      "values": [
            |        {
            |          "proto3_": {
            |            "path": "PARENT"
            |          },
            |          "name": "parent",
            |          "number": 0
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newProto3TestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("/test.main/address_book_relationship/parent number 0 does not match person.proto:/Relationship/PARENT number 1")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Proto3 mapping validation must fail for mapped enumeration with unmapped values`() {
        val testPackageJson = """
            |{
            |  "name": "test.main",
            |  "enumerations": [
            |    {
            |      "proto3_": {
            |        "path": "person.proto:/Relationship"
            |      },
            |      "name": "address_book_relationship",
            |      "values": [
            |        {
            |          "name": "parent",
            |          "number": 0
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newProto3TestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("/test.main/address_book_relationship/parent is not mapped but parent is mapped")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Proto3 mapping validation must fail for invalid entity mapping`() {
        val testPackageJson = """
            |{
            |  "name": "test.main",
            |  "entities": [
            |    {
            |      "proto3_": {
            |        "path": "address_book.proto:/Invalid/Path"
            |      },
            |      "name": "address_book_root",
            |      "fields": [
            |        {
            |          "proto3_": {
            |            "path": "name"
            |          },
            |          "name": "name",
            |          "number": 1,
            |          "type": "string"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newProto3TestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("/test.main/address_book_root/name mapping address_book.proto:/Invalid/Path/name does not exist")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Proto3 mapping validation must fail for invalid field mapping`() {
        val testPackageJson = """
            |{
            |  "name": "test.main",
            |  "entities": [
            |    {
            |      "proto3_": {
            |        "path": "address_book.proto:/Root"
            |      },
            |      "name": "address_book_root",
            |      "fields": [
            |        {
            |          "proto3_": {
            |            "path": "invalid_field"
            |          },
            |          "name": "name",
            |          "number": 1,
            |          "type": "string"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newProto3TestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("/test.main/address_book_root/name mapping address_book.proto:/Root/invalid_field does not exist")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Proto3 mapping validation must fail for invalid field number`() {
        val testPackageJson = """
            |{
            |  "name": "test.main",
            |  "entities": [
            |    {
            |      "proto3_": {
            |        "path": "address_book.proto:/Root"
            |      },
            |      "name": "address_book_root",
            |      "fields": [
            |        {
            |          "proto3_": {
            |            "path": "name"
            |          },
            |          "name": "name",
            |          "number": 2,
            |          "type": "string"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newProto3TestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("/test.main/address_book_root/name number 2 does not match address_book.proto:/Root/name number 1")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Proto3 mapping validation must fail for mapped enumeration field with unmapped field type`() {
        val testPackageJson = """
            |{
            |  "name": "test.main",
            |  "entities": [
            |    {
            |      "proto3_": {
            |        "path": "address_book.proto:/Root"
            |      },
            |      "name": "address_book_root",
            |      "fields": [
            |        {
            |          "proto3_": {},
            |          "name": "type",
            |          "number": 2,
            |          "type": "enumeration",
            |          "enumeration": {
            |            "name": "enumeration1",
            |            "package": "org.tree_ware.test.common"
            |          }
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newProto3TestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("/test.main/address_book_root/type enumeration field is mapped but enumeration is not mapped")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Proto3 mapping validation must fail for mapped composition field with unmapped field type`() {
        val testPackageJson = """
            |{
            |  "name": "test.main",
            |  "entities": [
            |    {
            |      "proto3_": {
            |        "path": "address_book.proto:/Root"
            |      },
            |      "name": "address_book_root",
            |      "fields": [
            |        {
            |          "proto3_": {},
            |          "name": "settings",
            |          "number": 2,
            |          "type": "composition",
            |          "composition": {
            |            "entity": "entity1",
            |            "package": "org.tree_ware.test.common"
            |          }
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val (metaModel, metaModelErrors) = newProto3TestMetaModel(testPackageJson)
        assertNull(metaModel)
        val expectedErrors =
            listOf("/test.main/address_book_root/settings composition field is mapped but entity is not mapped")
        assertEquals(expectedErrors.joinToString("\n"), metaModelErrors.joinToString("\n"))
    }

    @Test
    fun `Proto3 mapping validation must pass for valid mappings`() {
        val proto3Mappings = getProto3Mappings(proto3AddressBookMetaModel)
        val expected = readFile("validation/address_book_absolute_paths.txt")
        val actual = proto3Mappings.joinToString("\n")
        assertEquals(expected, actual)
    }
}

private fun newProto3TestMetaModel(testPackageJson: String): ValidatedMetaModel = newMetaModelFromJsonStrings(
    listOf(newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)),
    false,
    null,
    null,
    ::defaultRootEntityFactory,
    listOf(Proto3MetaModelMapAuxPlugin(PROTO_DESCRIPTOR_FILE)),
    true
)

private fun getProto3Mappings(metaModel: EntityModel): List<String> {
    val visitor = GetProto3MappingsVisitor()
    metaModelForEach(metaModel, visitor)
    return visitor.mappings
}

private class GetProto3MappingsVisitor :
    AbstractLeader1MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val mappings = mutableListOf<String>()

    override fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel): TraversalAction {
        val aux = getProto3MetaModelMap(leaderEnumerationValueMeta1)
        val fullName = getMetaModelResolved(leaderEnumerationValueMeta1)?.fullName
        aux?.validated?.also { mappings.add("$fullName -> ${it.path} = ${it.fieldNumber}") }
        return TraversalAction.CONTINUE
    }

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        val aux = getProto3MetaModelMap(leaderFieldMeta1)
        val fullName = getMetaModelResolved(leaderFieldMeta1)?.fullName
        aux?.validated?.also { mappings.add("$fullName -> ${it.path} = ${it.fieldNumber}") }
        return TraversalAction.CONTINUE
    }
}