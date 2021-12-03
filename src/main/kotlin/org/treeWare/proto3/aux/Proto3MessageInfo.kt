package org.treeWare.proto3.aux

import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.getAux

private const val PROTO3_MESSAGE_INFO_AUX_NAME = "proto3_message_info"

data class Proto3MessageInfo(val serializedSize: Int)

fun getProto3MessageInfo(element: ElementModel?): Proto3MessageInfo? =
    element?.getAux<Proto3MessageInfo>(PROTO3_MESSAGE_INFO_AUX_NAME)

fun setProto3MessageInfo(element: ElementModel, aux: Proto3MessageInfo) {
    element.setAux(PROTO3_MESSAGE_INFO_AUX_NAME, aux)
}