package sign

import com.syntifi.crypto.key.encdec.Hex

const val NOT_IMPLEMENTED_MSG = "Deliberately not implemented because it's unnecessary"

class CasperSigner(private val privateKey: String) : Signer {
    override fun sign(msg: String): String {
        val privateKey = Ed25519NoumenaPrivateKey(privateKey.byteInputStream())

        val signature = privateKey.sign(msg.toByteArray())

        return Hex.encode(signature)
    }
}
