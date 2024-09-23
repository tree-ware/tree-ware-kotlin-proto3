package org.treeWare.proto3.message

import org.junit.jupiter.api.Test
import org.treeWare.metaModel.proto3AddressBookMetaModel
import kotlin.test.assertTrue

class Proto3GenerationTests {

    @Test
    fun `Proto3 encoder must create proto files from metaModel`() {
        val writeDirectoryName = "generated/proto"
        encodeProto3(proto3AddressBookMetaModel, writeDirectoryName)
        val writePath = "generated/proto/org/treeWare/test/addressBook"
        val testPath = "src/test/resources/metaModel/proto/addressBook"
        assertTrue(directoriesEqual(writePath, testPath))
    }

    private fun directoriesEqual(filePath1: String, filePath2: String): Boolean {
        val returnValue = Runtime.getRuntime().exec("diff $filePath1 $filePath2").waitFor()
        return returnValue == 0
    }
}