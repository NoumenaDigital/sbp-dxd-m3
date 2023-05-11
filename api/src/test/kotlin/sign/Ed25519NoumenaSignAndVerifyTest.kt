package sign

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class Ed25519NoumenaSignAndVerifyTest {
    @Test
    fun `sign and verify using public and private keys`() {
        // given
        val msg = "Message to be signed".toByteArray()

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

        val privateKey = Ed25519NoumenaPrivateKey(privateKeyStr.byteInputStream())
        val publicKey = Ed25519NoumenaPublicKey(publicKeyStr.byteInputStream())

        // when
        val signature = privateKey.sign(msg)

        // then
        assertTrue { publicKey.verify(msg, signature) }
    }
}
