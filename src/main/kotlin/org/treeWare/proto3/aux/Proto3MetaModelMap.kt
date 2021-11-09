package org.treeWare.proto3.aux

class Proto3MetaModelMap(val path: String) {
    var validated: Proto3MetaModelMapValidated? = null
}

data class Proto3MetaModelMapValidated(val path: String)
