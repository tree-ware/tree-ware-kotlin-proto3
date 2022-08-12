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


private class ProtoEncoderVisitor(val writePath: String) :
    Leader1MetaModelVisitor<TraversalAction> {

    var headerWriter = PicoWriter() //handles syntax and package
    var importWriter = PicoWriter() //handles imports
    var bodyWriter = PicoWriter() //handles everything that comes after imports
    var currentPackage = ""

    val keyOptions = mutableListOf<String>()
    val requiredOptions = mutableListOf<String>()
    val nonPackageImports = mutableListOf<String>()

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
        if (bodyWriter.toString() != "") {
            /** Handle the actual writing to a file **/
            val packageName = getMetaName(leaderPackageMeta1)
            val filename = generateFileName(packageName)
            val outFile = File("$writePath/$filename")
            val outWriter = outFile.bufferedWriter()
            outWriter.write(headerWriter.toString())
            outWriter.write(importWriter.toString())
            outWriter.write(bodyWriter.toString())
            outWriter.close()
        }
        /** clear writers **/
        headerWriter = PicoWriter()
        importWriter = PicoWriter()
        bodyWriter = PicoWriter()
    }

    override fun visitEnumerationMeta(leaderEnumerationMeta1: EntityModel): TraversalAction {
        if (getProto3MetaModelMap(leaderEnumerationMeta1)?.path == null) {
            bodyWriter.writeln("")
            val enumName = getMetaName(leaderEnumerationMeta1)
            bodyWriter.writeln_r("enum ${snakeToCamel(enumName)} {")
            return TraversalAction.CONTINUE
        }
        return TraversalAction.ABORT_SUB_TREE
    }

    override fun leaveEnumerationMeta(leaderEnumerationMeta1: EntityModel) {
        if (getProto3MetaModelMap(leaderEnumerationMeta1)?.path == null) {
            bodyWriter.writeln_l("}")
        }
    }

    override fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel): TraversalAction {
        val enumValue = getMetaName(leaderEnumerationValueMeta1).uppercase()
        val enumNumber = getMetaNumber(leaderEnumerationValueMeta1)
        bodyWriter.writeln("$enumValue = $enumNumber;")
        return TraversalAction.CONTINUE
    }

    override fun leaveEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel) {

    }

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): TraversalAction {
        if (getProto3MetaModelMap(leaderEntityMeta1)?.path == null) {
            bodyWriter.writeln("")
            val entityName = getMetaName(leaderEntityMeta1)
            bodyWriter.writeln_r("message ${snakeToCamel(entityName)} {")
            visitMessageOptions(leaderEntityMeta1)
            return TraversalAction.CONTINUE
        }
        return TraversalAction.ABORT_SUB_TREE
    }

    override fun leaveEntityMeta(leaderEntityMeta1: EntityModel) {
        if (getProto3MetaModelMap(leaderEntityMeta1)?.path == null) {
            bodyWriter.writeln_l("}")
        }
    }

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        val fieldIndex = getMetaNumber(leaderFieldMeta1)
        val name = snakeToLowerCamel(getMetaName(leaderFieldMeta1))
        val type = getEncodingType(leaderFieldMeta1)
        var repeat = ""
        var fromPackage = ""

        if (getMultiplicityMeta(leaderFieldMeta1) == Multiplicity.LIST ||
            getMultiplicityMeta(leaderFieldMeta1) == Multiplicity.SET
        ) repeat = "repeated "

        val fieldType = getFieldTypeMeta(leaderFieldMeta1)
        if (fieldType == FieldType.COMPOSITION || fieldType == FieldType.ENUMERATION) {
            val resolvedEntity: EntityModel =
                if (fieldType == FieldType.COMPOSITION)
                    checkNotNull(getMetaModelResolved(leaderFieldMeta1)?.compositionMeta)
                else
                    checkNotNull(getMetaModelResolved(leaderFieldMeta1)?.enumerationMeta)

            val packageName = getPackageName(resolvedEntity)
            if (packageName != currentPackage) {
                val protoPackageName = generateFileName(packageName)
                val importLine = "import \"$protoPackageName\";"
                if (!importWriter.toString().contains(importLine))
                    importWriter.writeln(importLine)
                fromPackage = "$packageName."
            }
        }

        bodyWriter.write("$repeat$fromPackage$type $name = $fieldIndex")
        visitFieldOptions(leaderFieldMeta1)


        return TraversalAction.CONTINUE
    }

    override fun leaveFieldMeta(leaderFieldMeta1: EntityModel) {
    }

    /** Helper functions below this point **/

    fun visitMessageOptions(leaderFieldMeta: EntityModel) {
        getProto3MetaModelMap(leaderFieldMeta)?.options?.forEach { option ->
            bodyWriter.writeln("option $option;")
        }
    }

    fun visitFieldOptions(leaderFieldMeta: EntityModel) {
        var hasWrittenFirstOption = false
        var options = mutableListOf<String>()

        if (isKeyFieldMeta(leaderFieldMeta)) {
            options.addAll(keyOptions)
        } else if (isRequiredFieldMeta(leaderFieldMeta)) {
            options.addAll(requiredOptions)
        }

        if (getProto3MetaModelMap(leaderFieldMeta)?.options != null) {
            options.addAll(checkNotNull(getProto3MetaModelMap(leaderFieldMeta)?.options))
        }

        options.forEach { option ->
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
            val importString = "import \"$import\";"
            nonPackageImports.add(importString)
        }
    }

    fun writeImports() {
        if (nonPackageImports.isNotEmpty()) {
            nonPackageImports.forEach {
                importWriter.writeln(it)
            }
        }
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

    /** This takes a package name and then returns the relative path to a proto file **/
    // It also creates the directory if it doesn't exist
    fun generateFileName(packageName: String): String {
        var fullName = packageName.split(".")
        val camelName = fullName.map { snakeToLowerCamel(it) }
        val fileName = camelName.last()
        val directoryName = camelName.dropLast(1).joinToString("/")
        val fullDirectoryPath = "$writePath/$directoryName"
        val directory = File(fullDirectoryPath)
        if (!directory.exists()) directory.mkdirs()
        return "$directoryName/$fileName.proto"
    }
}