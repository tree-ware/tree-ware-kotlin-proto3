package org.treeWare.proto3.aux

import org.treeWare.metaModel.aux.MetaModelAuxPlugin
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachineFactory
import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.proto3.validation.validate

class Proto3MetaModelAuxPlugin(private val protoDescriptorFile: String) : MetaModelAuxPlugin {
    override val auxName: String = PROTO3_META_MODEL_MAP_CODEC_AUX_NAME
    override val auxDecodingStateMachineFactory: AuxDecodingStateMachineFactory = { Proto3MetaModelMapStateMachine(it) }
    override val auxEncoder: AuxEncoder = Proto3MetaModelMapEncoder()

    override fun validate(mainMeta: MutableMainModel): List<String> = validate(mainMeta, protoDescriptorFile)
}