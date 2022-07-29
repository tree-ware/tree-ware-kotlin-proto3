package org.treeWare.proto3.message

import com.google.protobuf.ByteString
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import org.treeWare.model.core.MainModel
import org.treeWare.proto3.aux.getProto3MessageInfo
import java.io.OutputStream

/**
 * Adapts a model into a protobuf message (for serialization only).
 *
 * WARNING: Only fields mapped in the meta-model will be serialized.
 * WARNING: only the methods required for serialization have been implemented.
 * WARNING: the message can be serialized multiple times as long as the underlying
 * model does not change. If the underlying model is changed (after a serialization),
 * create a new instance of this class with the changed model.
 *
 * Proto3 wire format: https://developers.google.com/protocol-buffers/docs/encoding
 */
class Proto3MessageAdapter(private val mainModel: MainModel) : MessageLite {
    private var isMemoized = false

    override fun getDefaultInstanceForType(): MessageLite = throw UnsupportedOperationException()

    override fun isInitialized(): Boolean = throw UnsupportedOperationException()

    override fun writeTo(output: CodedOutputStream) {
        serialize(mainModel, output)
    }

    override fun writeTo(output: OutputStream) {
        val bufferSize = serializedSize.takeIf { it <= CodedOutputStream.DEFAULT_BUFFER_SIZE }
            ?: CodedOutputStream.DEFAULT_BUFFER_SIZE
        val codedOutput = CodedOutputStream.newInstance(output, bufferSize)
        writeTo(codedOutput)
        codedOutput.flush()
    }

    override fun getSerializedSize(): Int {
        if (!isMemoized) {
            computeSerializedSize(mainModel)
            isMemoized = true
        }
        return getProto3MessageInfo(mainModel)?.serializedSize ?: 0
    }

    override fun getParserForType(): Parser<out MessageLite> = throw UnsupportedOperationException()

    override fun toByteString(): ByteString = throw UnsupportedOperationException()

    override fun toByteArray(): ByteArray {
        val array = ByteArray(serializedSize)
        val codedOutput = CodedOutputStream.newInstance(array)
        writeTo(codedOutput)
        val spaceLeft = codedOutput.spaceLeft()
        if (spaceLeft != 0) {
            throw IllegalStateException("Expected $serializedSize bytes in coded output; $spaceLeft bytes missing")
        }
        return array
    }

    override fun writeDelimitedTo(output: OutputStream) = throw UnsupportedOperationException()

    override fun newBuilderForType(): MessageLite.Builder = throw UnsupportedOperationException()

    override fun toBuilder(): MessageLite.Builder = throw UnsupportedOperationException()
}