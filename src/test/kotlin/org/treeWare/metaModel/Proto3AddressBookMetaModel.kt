package org.treeWare.metaModel

import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MainModel
import org.treeWare.proto3.aux.Proto3MetaModelAuxPlugin

val PROTO3_ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "metaModel/address_book_root.json",
    "metaModel/address_book_main.json",
    "metaModel/address_book_city.json",
)

fun newProto3AddressBookMetaModel(hasher: Hasher?, cipher: Cipher?, protoDescriptorFile: String): MainModel =
    newMetaModel(
        PROTO3_ADDRESS_BOOK_META_MODEL_FILES,
        false,
        hasher,
        cipher,
        listOf(Proto3MetaModelAuxPlugin(protoDescriptorFile))
    )