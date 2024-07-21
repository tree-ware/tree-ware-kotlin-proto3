package org.treeWare.proto3.message

import addressBook.AddressBook
import addressBook.PersonOuterClass
import com.google.protobuf.ByteString
import org.treeWare.metaModel.proto3AddressBookMetaModel
import org.treeWare.model.getMainModelFromJsonString
import kotlin.test.Test
import kotlin.test.assertEquals

private fun ByteArray.toMultiLineString() = this.joinToString("\n")

class Proto3MessageAdapterTests {
    @Test
    fun `Proto3 message adapter must serialize roots`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "last_updated": "1587147731"
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        root.lastUpdated = 1587147731
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must serialize single compositions`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "settings": {
            |       "encrypt_hero_name": true
            |     }
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        val settings = root.advancedSettingsBuilder
        settings.encryptHeroName = true
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must serialize single enumerations`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "settings": {
            |       "background_color": "yellow"
            |     }
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        val settings = root.advancedSettingsBuilder
        settings.backgroundColor = AddressBook.Settings.Color.YELLOW
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must not serialize unmapped single enumerations`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "settings": {
            |       "background_color": "yellow",
            |       "menu_color": "indigo"
            |     }
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        val settings = root.advancedSettingsBuilder
        settings.backgroundColor = AddressBook.Settings.Color.YELLOW
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must serialize enumeration lists`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "settings": {
            |       "card_colors": [
            |         {
            |           "value": "orange"
            |         },
            |         {
            |           "value": "green"
            |         },
            |         {
            |           "value": "blue"
            |         }
            |       ]
            |     }
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        val settings = root.advancedSettingsBuilder
        settings.addCardColors(AddressBook.Settings.Color.ORANGE)
        settings.addCardColors(AddressBook.Settings.Color.GREEN)
        settings.addCardColors(AddressBook.Settings.Color.BLUE)
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must serialize composition sets`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "person": [
            |       {
            |         "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |         "first_name": "Clark",
            |         "last_name": "Kent",
            |         "hero_name": "Superman"
            |       },
            |       {
            |         "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |         "first_name": "Lois",
            |         "last_name": "Lane"
            |       }
            |     ]
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        val person1 = root.addPersonBuilder()
        person1.id = "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
        person1.firstName = "Clark"
        person1.lastName = "Kent"
        person1.heroName = "Superman"
        val person2 = root.addPersonBuilder()
        person2.id = "a8aacf55-7810-4b43-afe5-4344f25435fd"
        person2.firstName = "Lois"
        person2.lastName = "Lane"
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must serialize enumerations inside composition sets`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "person": [
            |       {
            |         "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |         "first_name": "Clark",
            |         "relation": [
            |           {
            |             "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |             "relationship": "colleague"
            |           }
            |         ]
            |       },
            |       {
            |         "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |         "first_name": "Lois",
            |         "relation": [
            |           {
            |             "id": "16634916-8f83-4376-ad42-37038e108a0b",
            |             "relationship": "colleague"
            |           }
            |         ]
            |       }
            |     ]
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        val person1 = root.addPersonBuilder()
        person1.id = "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
        person1.firstName = "Clark"
        val person1Relation = person1.addRelationBuilder()
        person1Relation.id = "05ade278-4b44-43da-a0cc-14463854e397"
        person1Relation.relationship = PersonOuterClass.Relationship.COLLEAGUE
        val person2 = root.addPersonBuilder()
        person2.id = "a8aacf55-7810-4b43-afe5-4344f25435fd"
        person2.firstName = "Lois"
        val person2Relation = person2.addRelationBuilder()
        person2Relation.id = "16634916-8f83-4376-ad42-37038e108a0b"
        person2Relation.relationship = PersonOuterClass.Relationship.COLLEAGUE
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must serialize string lists`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "person": [
            |       {
            |         "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |         "email": [
            |           {
            |             "value": "clark.kent@dailyplanet.com"
            |           },
            |           {
            |             "value": "superman@dc.com"
            |           }
            |         ]
            |       },
            |       {
            |         "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |         "email": [
            |           {
            |             "value": "lois.lane@dailyplanet.com"
            |           }
            |         ]
            |       }
            |     ]
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        val person1 = root.addPersonBuilder()
        person1.id = "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
        person1.addEmail("clark.kent@dailyplanet.com")
        person1.addEmail("superman@dc.com")
        val person2 = root.addPersonBuilder()
        person2.id = "a8aacf55-7810-4b43-afe5-4344f25435fd"
        person2.addEmail("lois.lane@dailyplanet.com")
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must serialize blobs`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "person": [
            |       {
            |         "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |         "picture": "UGljdHVyZSBvZiBDbGFyayBLZW50"
            |       },
            |       {
            |         "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |         "picture": "UGljdHVyZSBvZiBMb2lzIExhbmU="
            |       }
            |     ]
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        val person1 = root.addPersonBuilder()
        person1.id = "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
        person1.picture = ByteString.copyFromUtf8("Picture of Clark Kent")
        val person2 = root.addPersonBuilder()
        person2.id = "a8aacf55-7810-4b43-afe5-4344f25435fd"
        person2.picture = ByteString.copyFromUtf8("Picture of Lois Lane")
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must serialize empty entities`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "settings": {
            |     }
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        root.advancedSettingsBuilder
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must not serialize empty lists`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "settings": {
            |       "card_colors": []
            |     },
            |     "person": [
            |       {
            |         "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |         "email": []
            |       }
            |     ]
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        val settings = root.advancedSettingsBuilder
        settings.clearCardColors()
        val person1 = root.addPersonBuilder()
        person1.id = "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
        person1.clearEmail()
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    @Test
    fun `Proto3 message adapter must not serialize empty sets`() {
        val modelJson = """
            | {
            |   "address_book": {
            |     "name": "Super Heroes",
            |     "person": []
            |   }
            | }
        """.trimMargin()
        val model = getMainModelFromJsonString(proto3AddressBookMetaModel, modelJson)

        // Build the expected message using generated code.
        val root = AddressBook.Root.newBuilder()
        root.name = "Super Heroes"
        root.clearPerson()
        val expectedMessage = root.build()

        val treeWareMessage = Proto3MessageAdapter(model)

        val expectedSize = expectedMessage.serializedSize
        val actualSize = treeWareMessage.serializedSize
        assertEquals(expectedSize, actualSize)

        val expectedBytes = expectedMessage.toByteArray().joinToString("\n")
        val actualBytes = treeWareMessage.toByteArray().joinToString("\n")
        assertEquals(expectedBytes, actualBytes)
    }

    // TODO(#17): fun `Proto3 message adapter must serialize nested model to flat protobuf`() {}

    // TODO(#17): fun `Proto3 message adapter must not serialize default values`() {}
    // TODO(#17): fun `Proto3 message adapter must not serialize unmapped model elements`() {}

    // TODO(#17): fun `Proto3 message adapter must serialize a model with all element types`() {}
    // TODO(#17): fun `Proto3 message adapter must be serializable multiple times`() {}
}