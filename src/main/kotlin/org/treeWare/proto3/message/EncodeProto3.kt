package org.treeWare.proto3.message

import okio.FileSystem
import okio.Path.Companion.toPath
import org.ainslec.picocog.PicoWriter
import org.treeWare.metaModel.*
import org.treeWare.metaModel.traversal.Leader1MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.core.getMetaModelResolved
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.proto3.aux.Proto3MetaModelMap
import org.treeWare.proto3.aux.getProto3MetaModelMap

private const val INDENT = "  "

fun encodeProto3(mainMetaModel: MainModel, writePath: String) {
    val protoVisitor = ProtoEncoderVisitor(writePath)
    metaModelForEach(mainMetaModel, protoVisitor)
}

private class ProtoEncoderVisitor(val writePath: String) :
    Leader1MetaModelVisitor<TraversalAction> {

    var headerWriter = PicoWriter(INDENT) //handles syntax and package
    var importWriter = PicoWriter(INDENT) //handles imports
    var optionsWriter = PicoWriter(INDENT)
    var bodyWriter = PicoWriter(INDENT) //handles everything that comes after imports
    var currentPackage = ""

    var mainMetaModelMap: Proto3MetaModelMap? = null

    override fun visitMainMeta(leaderMainMeta1: MainModel): TraversalAction {
        mainMetaModelMap = getProto3MetaModelMap(leaderMainMeta1)
        return TraversalAction.CONTINUE
    }

    override fun leaveMainMeta(leaderMainMeta1: MainModel) {
    }

    override fun visitVersionMeta(leaderVersionMeta1: EntityModel): TraversalAction {
        return TraversalAction.CONTINUE
    }

    override fun leaveVersionMeta(leaderVersionMeta1: EntityModel) {
    }

    override fun visitRootMeta(leaderRootMeta1: EntityModel): TraversalAction {
        return TraversalAction.CONTINUE
    }

    override fun leaveRootMeta(leaderRootMeta1: EntityModel) {
    }

    override fun visitPackageMeta(leaderPackageMeta1: EntityModel): TraversalAction {
        writeMainImports()
        writeMainOptions()
        val name = getMetaName(leaderPackageMeta1)
        currentPackage = name
        headerWriter.writeln("syntax = \"proto3\";")
        headerWriter.writeln("")
        headerWriter.writeln("package ${getProto3PackageName(name)};")
        headerWriter.writeln("")

        return TraversalAction.CONTINUE
    }

    override fun leavePackageMeta(leaderPackageMeta1: EntityModel) {
        if (bodyWriter.toString() != "") {
            /** Handle the actual writing to a file **/
            val packageName = getMetaName(leaderPackageMeta1)
            val filename = generateFileName(packageName)
            FileSystem.SYSTEM.write("$writePath/$filename".toPath()) {
                this.writeUtf8("// AUTO-GENERATED FILE. DO NOT EDIT.\n\n")
                this.writeUtf8(headerWriter.toString())
                this.writeUtf8(importWriter.toString())
                if (!optionsWriter.isEmpty) this.writeUtf8("\n")
                this.writeUtf8(optionsWriter.toString())
                this.writeUtf8(bodyWriter.toString())
            }
        }
        /** clear writers **/
        headerWriter = PicoWriter(INDENT)
        importWriter = PicoWriter(INDENT)
        optionsWriter = PicoWriter(INDENT)
        bodyWriter = PicoWriter(INDENT)
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
        val enumName = getMetaName(getParentEnumerationMeta(leaderEnumerationValueMeta1)).uppercase()
        val enumValue = getMetaName(leaderEnumerationValueMeta1).uppercase()
        val enumNumber = getMetaNumber(leaderEnumerationValueMeta1)
        bodyWriter.writeln("${enumName}_$enumValue = $enumNumber;")
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
                fromPackage = "${getProto3PackageName(packageName)}."
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
        val options = mutableListOf<String>()

        if (isKeyFieldMeta(leaderFieldMeta)) {
            mainMetaModelMap?.keyFieldOptions?.also { options.addAll(it) }
        } else if (isUnconditionallyRequiredFieldMeta(leaderFieldMeta)) {
            mainMetaModelMap?.requiredFieldOptions?.also { options.addAll(it) }
        }

        val fieldOptions = getProto3MetaModelMap(leaderFieldMeta)?.options
        if (fieldOptions != null) options.addAll(fieldOptions)

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

    fun writeMainImports() {
        mainMetaModelMap?.imports?.forEach { importWriter.writeln("import \"$it\";") }
    }

    fun writeMainOptions() {
        mainMetaModelMap?.options?.forEach { optionsWriter.writeln("option $it;") }
    }

    fun getProto3PackageName(treeWarePackageName: String): String =
        treeWarePackageName.split(".").joinToString(".") { snakeToLowerCamel(it) }

    /** This takes a package name and then returns the relative path to a proto file **/
    // It also creates the directory if it doesn't exist
    fun generateFileName(packageName: String): String {
        val fullName = packageName.split(".")
        val camelName = fullName.map { snakeToLowerCamel(it) }
        val fileName = camelName.last()
        val directoryName = camelName.dropLast(1).joinToString("/")
        val fullDirectoryPath = "$writePath/$directoryName"
        FileSystem.SYSTEM.createDirectories(fullDirectoryPath.toPath())
        return "$directoryName/$fileName.proto"
    }
}