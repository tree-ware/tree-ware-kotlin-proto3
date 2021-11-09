package org.treeWare.proto3.aux

import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.getAux

fun getProto3MetaModelMap(element: ElementModel?): Proto3MetaModelMap? =
    element?.getAux<Proto3MetaModelMap>(PROTO3_META_MODEL_MAP_CODEC_AUX_NAME)