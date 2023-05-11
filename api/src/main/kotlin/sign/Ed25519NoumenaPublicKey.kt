package sign

import com.syntifi.crypto.key.ASN1Identifiers
import com.syntifi.crypto.key.AbstractPublicKey
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.crypto.Signer
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.util.io.pem.PemReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Based on [com.syntifi.crypto.key.Ed25519PublicKey]
 */
class Ed25519NoumenaPublicKey(inputStream: InputStream) : AbstractPublicKey() {

    private lateinit var publicKeyParameters: Ed25519PublicKeyParameters

    init {
        val prKeyPemParsed: ByteArray = InputStreamReader(inputStream).use { keyReader ->
            PemReader(keyReader).use { pemReader ->
                val pemObject = pemReader.readPemObject()
                pemObject.content
            }
        }

        val aSN1Key = ASN1Primitive.fromByteArray(prKeyPemParsed)
        val objBaseSeq = ASN1Sequence.getInstance(aSN1Key)
        val algoId =
            ASN1ObjectIdentifier.getInstance(ASN1Sequence.getInstance(objBaseSeq.getObjectAt(0)).getObjectAt(0)).id

        if (algoId == ASN1Identifiers.Ed25519OID.id) {
            val dERBitStringKey = DERBitString.getInstance(objBaseSeq.getObjectAt(1))
            publicKeyParameters = Ed25519PublicKeyParameters(dERBitStringKey.bytes, 0)
            key = publicKeyParameters.encoded
        }
    }

    override fun loadPublicKey(publicKey: ByteArray) = throw RuntimeException(NOT_IMPLEMENTED_MSG)

    override fun readPublicKey(filename: String) = throw RuntimeException(NOT_IMPLEMENTED_MSG)

    override fun writePublicKey(filename: String) = throw RuntimeException(NOT_IMPLEMENTED_MSG)

    override fun verify(message: ByteArray, signature: ByteArray): Boolean {
        val verifier: Signer = Ed25519Signer()
        verifier.init(false, publicKeyParameters)
        verifier.update(message, 0, message.size)
        return verifier.verifySignature(signature)
    }
}
