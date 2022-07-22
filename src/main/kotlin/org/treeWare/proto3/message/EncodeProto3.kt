package org.treeWare.proto3.message

import org.ainslec.picocog.PicoWriter
import org.treeWare.metaModel.*
import org.treeWare.metaModel.traversal.Leader1MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.core.getMetaModelResolved
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.proto3.aux.getProto3MetaModelMap
import java.io.File


fun encodeProto3(mainMetaModel: MainModel, writePath: String) {
    val protoVisitor = ProtoEncoderVisitor(writePath)
    metaModelForEach(mainMetaModel, protoVisitor)
}


private class ProtoEncoderVisitor(val writePath: String) : Leader1MetaModelVisitor<TraversalAction> {

    var bodyWriter = PicoWriter()
    var headerWriter = PicoWriter()
    var currentPackage = ""

    val keyOptions = mutableListOf<String>()
    val requiredOptions = mutableListOf<String>()
    val imports = mutableListOf<String>()

    override fun visitMainMeta(leaderMainMeta1: MainModel): TraversalAction {
        visitImports(leaderMainMeta1)
        visitKeyFieldOptions(leaderMainMeta1)
        visitRequiredFieldOptions(leaderMainMeta1)

        return TraversalAction.CONTINUE
    }

    override fun leaveMainMeta(leaderMainMeta1: MainModel) {
    }

    override fun visitRootMeta(leaderRootMeta1: EntityModel): TraversalAction {
        return TraversalAction.CONTINUE
    }

    override fun leaveRootMeta(leaderRootMeta1: EntityModel) {
    }

    override fun visitPackageMeta(leaderPackageMeta1: EntityModel): TraversalAction {
        writeImports()
        val name = getMetaName(leaderPackageMeta1)
        currentPackage = name
        headerWriter.writeln("syntax = \"proto3\";")
        headerWriter.writeln("")
        headerWriter.writeln("package $name;")
        headerWriter.writeln("")

        return TraversalAction.CONTINUE
    }

    override fun leavePackageMeta(leaderPackageMeta1: EntityModel) {
        val name = getMetaName(leaderPackageMeta1)
        val fullPath = "$writePath/$name.proto"
        val outWriter = File(fullPath).bufferedWriter()
        outWriter.write(headerWriter.toString())
        outWriter.write(bodyWriter.toString())
        outWriter.close()
        bodyWriter = PicoWriter()
        headerWriter = PicoWriter()
    }

    override fun visitEnumerationMeta(leaderEnumerationMeta1: EntityModel): TraversalAction {
        val enumName = getMetaName(leaderEnumerationMeta1)
        bodyWriter.writeln_r("enum ${snakeToCamel(enumName)} {")
        return TraversalAction.CONTINUE
    }

    override fun leaveEnumerationMeta(leaderEnumerationMeta1: EntityModel) {
        bodyWriter.writeln_l("}")
        bodyWriter.writeln("")
    }

    override fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel): TraversalAction {
        val enumValue = getMetaName(leaderEnumerationValueMeta1).uppercase()
        val enumNumber = getMetaNumber(leaderEnumerationValueMeta1)
        bodyWriter.writeln("$enumValue = $enumNumber")
        return TraversalAction.CONTINUE
    }

    override fun leaveEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel) {

    }

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): TraversalAction {
        val entityName = getMetaName(leaderEntityMeta1)
        bodyWriter.writeln_r("message ${snakeToCamel(entityName)} {")
        visitMessageOptions(leaderEntityMeta1)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntityMeta(leaderEntityMeta1: EntityModel) {
        bodyWriter.writeln_l("}")
        bodyWriter.writeln()
    }

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        val fieldIndex = getMetaNumber(leaderFieldMeta1)
        val name = snakeToLowerCamel(getMetaName(leaderFieldMeta1))
        val type = getEncodingType(leaderFieldMeta1)
        var repeat = ""

        if (getMultiplicityMeta(leaderFieldMeta1) == Multiplicity.LIST ||
            getMultiplicityMeta(leaderFieldMeta1) == Multiplicity.SET
        ) repeat = "repeated "

        bodyWriter.write("$repeat$type $name = $fieldIndex")
        visitFieldOptions(leaderFieldMeta1)

        val fieldType = getFieldTypeMeta(leaderFieldMeta1)
        if (fieldType == FieldType.COMPOSITION || fieldType == FieldType.ENUMERATION) {
            val resolvedEntity: EntityModel =
                if (fieldType == FieldType.COMPOSITION) {
                    checkNotNull(getMetaModelResolved(leaderFieldMeta1)?.compositionMeta)
                } else {
                    checkNotNull(getMetaModelResolved(leaderFieldMeta1)?.enumerationMeta)
                }

            val packageName = getPackageName(resolvedEntity)
            if (packageName != currentPackage) {
                val importLine = "import \"$packageName.proto\";"
                if (!headerWriter.toString().contains(importLine))
                    headerWriter.writeln("import \"$packageName.proto\";")
            }
        }
        return TraversalAction.CONTINUE
    }

    override fun leaveFieldMeta(leaderFieldMeta1: EntityModel) {
    }

    fun visitMessageOptions(leaderFieldMeta: EntityModel) {
        getProto3MetaModelMap(leaderFieldMeta)?.options?.forEach { option ->
            bodyWriter.writeln("option $option;")
        }
    }

    fun visitFieldOptions(leaderFieldMeta: EntityModel) {
        var hasWrittenFirstOption = false

        if (isKeyFieldMeta(leaderFieldMeta)) {

            keyOptions.forEach { option ->
                val prefix = when (hasWrittenFirstOption) {
                    false -> {
                        hasWrittenFirstOption = true
                        " ["
                    }
                    true -> ", "
                }
                bodyWriter.write("$prefix$option")
            }
        } else if (isRequiredFieldMeta(leaderFieldMeta)) {
            requiredOptions.forEach { option ->
                val prefix = when (hasWrittenFirstOption) {
                    false -> {
                        hasWrittenFirstOption = true
                        " ["
                    }
                    true -> ", "
                }
                bodyWriter.write("$prefix$option")
            }
        }

        getProto3MetaModelMap(leaderFieldMeta)?.options?.forEach { option ->
            val prefix = when (hasWrittenFirstOption) {
                false -> {
                    hasWrittenFirstOption = true
                    " ["
                }
                true -> ", "
            }
            bodyWriter.write("$prefix$option")
        }
        if (hasWrittenFirstOption) bodyWriter.write("]")
        bodyWriter.writeln(";")
    }

    fun visitImports(leaderMeta: MainModel) {
        getProto3MetaModelMap(leaderMeta)?.imports?.forEach { import ->
            val importString = "import \"$import\""
            imports.add(importString)
        }
    }

    fun writeImports() {
        imports.forEach {
            bodyWriter.writeln(it)
        }
        bodyWriter.writeln("")
    }

    fun visitKeyFieldOptions(leaderMeta: MainModel) {
        getProto3MetaModelMap(leaderMeta)?.keyFieldOptions?.forEach { keyOption ->
            keyOptions.add(keyOption)
        }
    }

    fun visitRequiredFieldOptions(leaderMeta: MainModel) {
        getProto3MetaModelMap(leaderMeta)?.requiredFieldOptions?.forEach { requiredOption ->
            requiredOptions.add(requiredOption)
        }
    }
}