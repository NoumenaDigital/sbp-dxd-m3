package sign

import com.syntifi.crypto.key.ASN1Identifiers
import com.syntifi.crypto.key.AbstractPrivateKey
import com.syntifi.crypto.key.AbstractPublicKey
import com.syntifi.crypto.key.Ed25519PublicKey
import mu.KotlinLogging
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.crypto.Signer
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.util.io.pem.PemReader
import java.io.InputStream
import java.io.InputStreamReader

private val logger = KotlinLogging.logger {}

/**
 * Based on [com.syntifi.crypto.key.Ed25519PrivateKey]
 */
class Ed25519NoumenaPrivateKey(inputStream: InputStream) : AbstractPrivateKey() {

    private lateinit var privateKeyParameters: Ed25519PrivateKeyParameters

    init {
        val prKeyPemParsed: ByteArray = InputStreamReader(inputStream).use { keyReader ->
            PemReader(keyReader).use { pemReader ->
                val pemObject = pemReader.readPemObject()
                pemObject.content
            }
        }

        val aSN1Key = ASN1Primitive.fromByteArray(prKeyPemParsed)
        val keyInfo = PrivateKeyInfo.getInstance(aSN1Key)
        val algoId = keyInfo.privateKeyAlgorithm.algorithm.toString()

        if (algoId == ASN1Identifiers.Ed25519OID.id) {
            privateKeyParameters = Ed25519PrivateKeyParameters(keyInfo.privateKey.encoded, 4)
            key = privateKeyParameters.encoded
        }
    }

    override fun loadPrivateKey(privateKey: ByteArray) = throw RuntimeException(NOT_IMPLEMENTED_MSG)

    override fun readPrivateKey(filename: String) = throw RuntimeException(NOT_IMPLEMENTED_MSG)

    override fun writePrivateKey(filename: String) = throw RuntimeException(NOT_IMPLEMENTED_MSG)

    override fun sign(message: ByteArray): ByteArray {
        val signer: Signer = Ed25519Signer()
        signer.init(true, privateKeyParameters)
        signer.update(message, 0, message.size)

        return try {
            signer.generateSignature()
        } catch (e: Exception) {
            logger.error(e) { "sign message failed: ${e.message}" }
            throw e
        }
    }

    override fun derivePublicKey(): AbstractPublicKey {
        return Ed25519PublicKey(privateKeyParameters.generatePublicKey().encoded)
    }
}
