package org.treeWare.proto3.aux

import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.model.encoder.WireFormatEncoder

class Proto3MetaModelMapEncoder : AuxEncoder {
    override fun encode(fieldName: String?, auxName: String, aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        aux?.also {
            val auxFieldName = wireFormatEncoder.getAuxFieldName(fieldName, auxName)
            wireFormatEncoder.encodeObjectStart(auxFieldName)
            wireFormatEncoder.encodeStringField(PROTO3_META_MODEL_MAP_CODEC_PATH, (it as Proto3MetaModelMap).path)
            wireFormatEncoder.encodeObjectEnd()
        }
    }
}
