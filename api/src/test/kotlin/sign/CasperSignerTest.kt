package sign

import com.syntifi.crypto.key.encdec.Hex
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class CasperSignerTest {

    @Test
    fun `sign and verify using CasperSigner`() {
        // given
        val msg = "Message to be signed"

        val privateKeyStr = """
        -----BEGIN PRIVATE KEY-----
        MC4CAQAwBQYDK2VwBCIEIOIf4ykcG4DbQ3ob2oRc2SgQGhZ/Y3H1l7YTpAhRjwD1
        -----END PRIVATE KEY-----
        """.trimIndent()

        val publicKeyStr = """
        -----BEGIN PUBLIC KEY-----
        MCowBQYDK2VwAyEA6/EvcF3gNd5wXYu+xJgcJjR0ASs06FmqQaM3zJ0juxs=
        -----END PUBLIC KEY-----
        """.trimIndent()

        val publicKey = Ed25519NoumenaPublicKey(publicKeyStr.byteInputStream())

        val casperSigner = CasperSigner(privateKeyStr)

        // when
        val signature = casperSigner.sign(msg)

        // then
        assertTrue { publicKey.verify(msg.toByteArray(), Hex.decode(signature)) }
    }
}
