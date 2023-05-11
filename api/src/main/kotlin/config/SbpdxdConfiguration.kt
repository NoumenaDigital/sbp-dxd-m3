package config

import seed.config.IConfiguration
import java.net.URI

data class CasperCredential(
    val privateKey: String,
    val publicKey: String,
    val accountHash: String,
)

interface ISbpdxdConfiguration : IConfiguration {
    // Casper DID Contract
    val privateKeySbp: String
    val publicKeySbp: String
    val accountHashSbp: String

    val privateKeyUser1: String
    val publicKeyUser1: String
    val accountHashUser1: String

    val privateKeyUser2: String
    val publicKeyUser2: String
    val accountHashUser2: String

    val privateKeyUser3: String
    val publicKeyUser3: String
    val accountHashUser3: String

    val didMethod: String
    val casperNodeURL: String
    val casperNodePort: Int
    val casperChainName: String

    // Misc
    val storeURI: URI
    val infuraIpfsProjectId: String
    val infuraIpfsProjectSecret: String
    val includeIpfs: Boolean
    val disableContracts: Boolean
}

data class SbpdxdConfiguration(
    override val privateKeySbp: String = System.getenv("PRIVATE_KEY_SBP") ?: "",
    override val publicKeySbp: String = System.getenv("PUBLIC_KEY_SBP") ?: "",
    override val accountHashSbp: String = System.getenv("ACCOUNT_HASH_SBP") ?: "",

    override val privateKeyUser1: String = System.getenv("PRIVATE_KEY_USER_1") ?: "",
    override val publicKeyUser1: String = System.getenv("PUBLIC_KEY_USER_1") ?: "",
    override val accountHashUser1: String = System.getenv("ACCOUNT_HASH_USER_1") ?: "",

    override val privateKeyUser2: String = System.getenv("PRIVATE_KEY_USER_2") ?: "",
    override val publicKeyUser2: String = System.getenv("PUBLIC_KEY_USER_2") ?: "",
    override val accountHashUser2: String = System.getenv("ACCOUNT_HASH_USER_2") ?: "",

    override val privateKeyUser3: String = System.getenv("PRIVATE_KEY_USER_3") ?: "",
    override val publicKeyUser3: String = System.getenv("PUBLIC_KEY_USER_3") ?: "",
    override val accountHashUser3: String = System.getenv("ACCOUNT_HASH_USER_3") ?: "",

    override val didMethod: String = System.getenv("DID_METHOD") ?: "",
    override val casperNodeURL: String = System.getenv("CASPER_NODE_URL") ?: "",
    override val casperNodePort: Int = (System.getenv("CASPER_NODE_PORT") ?: "").toInt(),
    override val casperChainName: String = (System.getenv("CASPER_CHAIN_NAME")) ?: "",

    override val storeURI: URI = URI.create((System.getenv("IPFS_ENDPOINT") ?: "https://ipfs.infura.io:5001")),
    override val infuraIpfsProjectId: String = System.getenv("INFURA_IPFS_PROJECT_ID") ?: "",
    override val infuraIpfsProjectSecret: String = System.getenv("INFURA_IPFS_PROJECT_SECRET") ?: "",
    override val includeIpfs: Boolean = (System.getenv("INCLUDE_IPFS") ?: "true").toBoolean(),
    override val disableContracts: Boolean = (System.getenv("DISABLE_CONTRACT_ENDPOINTS") ?: "false").toBoolean(),

    private val baseConfig: IConfiguration,
) : ISbpdxdConfiguration, IConfiguration by baseConfig
