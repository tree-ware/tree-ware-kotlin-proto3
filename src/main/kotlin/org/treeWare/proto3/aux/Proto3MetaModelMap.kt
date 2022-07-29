package org.treeWare.proto3.aux

class Proto3MetaModelMap(
    val path: String?,
    val imports: List<String>?,
    val options: List<String>?,
    val keyFieldOptions: List<String>?,
    val requiredFieldOptions: List<String>?
) {
    var validated: Proto3MetaModelMapValidated? = null
}

data class Proto3MetaModelMapValidated(val path: String?, val fieldNumber: Int)