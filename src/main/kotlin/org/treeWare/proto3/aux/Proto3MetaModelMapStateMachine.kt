package org.treeWare.proto3.aux

import org.treeWare.model.decoder.stateMachine.*
import org.treeWare.util.assertInDevMode

class Proto3MetaModelMapStateMachine(
    private val stack: DecodingStack
) : AuxDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var aux: Proto3MetaModelMap? = null
    private var path: String? = null
    private var imports: List<String>? = null
    private var options: List<String>? = null
    private var keyFieldOptions: List<String>? = null
    private var requiredFieldOptions: List<String>? = null

    override fun newAux() {
        aux = null
    }

    override fun getAux(): Any? {
        return aux
    }

    override fun decodeKey(name: String): Boolean {
        super.decodeKey(name)
        return when (keyName) {
            PROTO3_META_MODEL_MAP_CODEC_PATH -> true
            PROTO3_META_MODEL_MAP_CODEC_IMPORTS -> {
                val list = mutableListOf<String>()
                imports = list
                stack.addFirst(StringListStateMachine(list, stack))
                true
            }
            PROTO3_META_MODEL_MAP_CODEC_OPTIONS -> {
                val list = mutableListOf<String>()
                options = list
                stack.addFirst(StringListStateMachine(list, stack))
                true
            }
            PROTO3_META_MODEL_MAP_CODEC_KEY_FIELD_OPTIONS -> {
                val list = mutableListOf<String>()
                keyFieldOptions = list
                stack.addFirst(StringListStateMachine(list, stack))
                true
            }
            PROTO3_META_MODEL_MAP_CODEC_REQUIRED_FIELD_OPTIONS -> {
                val list = mutableListOf<String>()
                requiredFieldOptions = list
                stack.addFirst(StringListStateMachine(list, stack))
                true
            }

            else -> {
                stack.addFirst(SkipUnknownStateMachine(stack))
                true
            }
        }
    }

    override fun decodeStringValue(value: String): Boolean {
        path = if (keyName != PROTO3_META_MODEL_MAP_CODEC_PATH) null else value
        return true
    }


    override fun decodeObjectStart(): Boolean {
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        aux = Proto3MetaModelMap(path, imports, options, keyFieldOptions, requiredFieldOptions)
        // Remove self from stack
        stack.removeFirst()
        return true
    }

    override fun decodeListStart(): Boolean {
        // This method should never get called
        assertInDevMode(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        // This method should never get called
        assertInDevMode(false)
        return false
    }
}
