package org.treeWare.proto3.message

import org.junit.jupiter.api.Test
import org.treeWare.metaModel.proto3AddressBookMetaModel
import java.io.StringWriter

class Proto3GenerationTests {

    @Test
    fun `Proto3 encoder must create a proto file from a metaModel`() {
        val writer = StringWriter()
        encodeProto3(proto3AddressBookMetaModel, "generated/proto")
        val protoString = writer.toString()
        assert(protoString == "")
    }
}