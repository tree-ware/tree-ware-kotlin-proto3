package org.treeWare.proto3.aux

import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.model.encoder.WireFormatEncoder

class Proto3MetaModelMapEncoder : AuxEncoder {
    override fun encode(fieldName: String?, auxName: String, aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        aux?.also { mapAux ->
            val auxFieldName = wireFormatEncoder.getAuxFieldName(fieldName, auxName)
            wireFormatEncoder.encodeObjectStart(auxFieldName)
            val metaModelMap = mapAux as Proto3MetaModelMap
            metaModelMap.path?.also { wireFormatEncoder.encodeStringField(PROTO3_META_MODEL_MAP_CODEC_PATH, it) }
            metaModelMap.imports?.also {
                encodeList(PROTO3_META_MODEL_MAP_CODEC_IMPORTS, it, wireFormatEncoder)
            }
            metaModelMap.options?.also {
                encodeList(PROTO3_META_MODEL_MAP_CODEC_OPTIONS, it, wireFormatEncoder)
            }
            metaModelMap.keyFieldOptions?.also {
                encodeList(PROTO3_META_MODEL_MAP_CODEC_KEY_FIELD_OPTIONS, it, wireFormatEncoder)
            }
            metaModelMap.requiredFieldOptions?.also {
                encodeList(PROTO3_META_MODEL_MAP_CODEC_REQUIRED_FIELD_OPTIONS, it, wireFormatEncoder)
            }
            wireFormatEncoder.encodeObjectEnd()
        }
    }
}

private fun encodeList(fieldName: String, list: List<String>, wireFormatEncoder: WireFormatEncoder) {
    wireFormatEncoder.encodeListStart(fieldName)
    list.forEach { wireFormatEncoder.encodeStringField("", it) }
    wireFormatEncoder.encodeListEnd()
}
