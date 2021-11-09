package org.treeWare.metaModel

import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.proto3.aux.PROTO3_META_MODEL_MAP_CODEC_AUX_NAME
import org.treeWare.proto3.aux.Proto3MetaModelMapStateMachine

val PROTO3_ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "metaModel/address_book_root.json",
    "metaModel/address_book_main.json",
    "metaModel/address_book_city.json",
)

fun newProto3AddressBookMetaModel(hasher: Hasher?, cipher: Cipher?): MutableMainModel =
    newMetaModelFromFiles(PROTO3_ADDRESS_BOOK_META_MODEL_FILES, hasher, cipher, MultiAuxDecodingStateMachineFactory(
        PROTO3_META_MODEL_MAP_CODEC_AUX_NAME to { Proto3MetaModelMapStateMachine(it) }
    ))
