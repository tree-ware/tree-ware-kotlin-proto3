package org.treeWare.proto3.aux

import org.treeWare.metaModel.PROTO3_ADDRESS_BOOK_META_MODEL_FILES
import org.treeWare.metaModel.newMainMetaMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

private val metaMetaModel = newMainMetaMetaModel()

class Proto3MetaModelMapTests {
    @Test
    fun `Proto3 meta-model JSON codec round trip must be lossless`() {
        val proto3MetaModelAuxPlugin = Proto3MetaModelAuxPlugin("build/generated/source/proto/test/descriptor_set.desc")
        PROTO3_ADDRESS_BOOK_META_MODEL_FILES.forEach { file ->
            testRoundTrip(
                file,
                multiAuxEncoder = MultiAuxEncoder(
                    proto3MetaModelAuxPlugin.auxName to proto3MetaModelAuxPlugin.auxEncoder
                ),
                multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                    proto3MetaModelAuxPlugin.auxName to proto3MetaModelAuxPlugin.auxDecodingStateMachineFactory
                ),
                metaModel = metaMetaModel
            )
        }
    }
}