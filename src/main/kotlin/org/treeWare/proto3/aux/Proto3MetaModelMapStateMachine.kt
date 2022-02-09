package org.treeWare.proto3.aux

import org.treeWare.model.decoder.stateMachine.AbstractDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.DecodingStack
import org.treeWare.util.assertInDevMode

class Proto3MetaModelMapStateMachine(
    private val stack: DecodingStack
) : AuxDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var aux: Proto3MetaModelMap? = null

    override fun newAux() {
        aux = null
    }

    override fun getAux(): Any? {
        return aux
    }

    override fun decodeStringValue(value: String): Boolean {
        if (keyName != PROTO3_META_MODEL_MAP_CODEC_PATH) return false
        aux = Proto3MetaModelMap(value)
        return true
    }

    override fun decodeObjectStart(): Boolean {
        return true
    }

    override fun decodeObjectEnd(): Boolean {
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
