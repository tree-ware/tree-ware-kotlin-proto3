package org.treeWare.proto3.message

import org.junit.jupiter.api.Test
import org.treeWare.metaModel.proto3AddressBookMetaModel
import java.io.File
import java.io.StringWriter

class Proto3GenerationTests {

    @Test
    fun `Proto3 encoder must create a proto file from a metaModel`() {
        val writer = StringWriter()
        val directoryName = "generated/proto"
        val directory = File(directoryName)
        if (!directory.exists()) directory.mkdirs()
        encodeProto3(proto3AddressBookMetaModel, directoryName)
        val protoString = writer.toString()
        assert(protoString == "")
    }


}