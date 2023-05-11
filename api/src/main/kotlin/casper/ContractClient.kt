package casper

import com.casper.sdk.CasperSdk
import com.casper.sdk.exceptions.ValueNotFoundException
import com.casper.sdk.service.serialization.cltypes.CLValueBuilder
import com.casper.sdk.service.serialization.util.ByteUtils
import com.casper.sdk.types.ContractHash
import com.casper.sdk.types.DeployNamedArgBuilder
import com.casper.sdk.types.DeployParams
import com.casper.sdk.types.StoredContractByHash
import com.fasterxml.jackson.databind.ObjectMapper
import config.ISbpdxdConfiguration
import mu.KotlinLogging
import java.math.BigInteger
import java.security.KeyPair
import java.security.PublicKey

private val logger = KotlinLogging.logger {}

fun interface ContractClient {
    fun addDelegate(cargo: AddDelegateCargo): String
}

class CasperContractClient(
    private val config: ISbpdxdConfiguration,
    private val casperSdk: CasperSdk = CasperSdk(config.casperNodeURL, config.casperNodePort),
    private val objectMapper: ObjectMapper = ObjectMapper(),
) : ContractClient {
    override fun addDelegate(cargo: AddDelegateCargo): String {
        val sbpKeyPair = getKeys(publicKey = config.publicKeySbp, privateKey = config.privateKeySbp)
        val contractHash = getContractHash(cargo.contractNamedKey, sbpKeyPair.public)
        val identityKeyPair = getKeys(publicKey = cargo.publicKeyIdentity, privateKey = cargo.privateKeyIdentity)
        val deployParams = getDeployParams(identityKeyPair.public)
        val payment = casperSdk.standardPayment(BigInteger(cargo.gas))

        val identityVal = CLValueBuilder.accountKey(ByteUtils.decodeHex(cargo.didOwnerAccountHash))
        val delegateTypeVal = CLValueBuilder.string(cargo.delegateType)
        val delegateVal = CLValueBuilder.accountKey(ByteUtils.decodeHex(cargo.delegateAccountHash))
        val validityVal = CLValueBuilder.u64(cargo.validity)

        val deploy = casperSdk.makeDeploy(
            deployParams,
            StoredContractByHash(
                contractHash,
                "add_delegate",
                DeployNamedArgBuilder()
                    .add("identity", identityVal)
                    .add("delegate_type", delegateTypeVal)
                    .add("delegate", delegateVal)
                    .add("validity", validityVal)
                    .build()
            ),
            payment
        )

        casperSdk.signDeploy(deploy, identityKeyPair)

        val response = casperSdk.putDeploy(deploy)

        logger.info { "Delegate added $cargo with response: $response" }

        return response.toString()
    }

    private fun getContractHash(
        contractNamedKey: String = "did_registry_sbp_v1_contract_hash",
        accountKey: PublicKey,
    ): ContractHash {
        val accountInfo = casperSdk.getAccountInfo(accountKey)

        val map = objectMapper.reader().readValue(accountInfo, Map::class.java)

        val storedValue = map["stored_value"] as Map<*, *>
        val account = storedValue["Account"] as Map<*, *>
        val namedKeys = account["named_keys"] as List<*>
        val namedKey = namedKeys.first { item -> (item as Map<*, *>)["name"] == contractNamedKey }

        if (namedKey is Map<*, *>) {
            val contractHash = namedKey["key"] as String
            if (contractHash.length > 5) {
                return ContractHash(ByteUtils.decodeHex(contractHash.substring(5)))
            }
        }
        throw ValueNotFoundException("'$contractNamedKey' not found in account info 'named_keys'")
    }

    private fun getDeployParams(publicKey: PublicKey) = DeployParams(
        publicKey,
        config.casperChainName,
        null,
        null,
        null,
        null
    )

    private fun getKeys(publicKey: String, privateKey: String): KeyPair {
        val casperPublicKey =
            """
            -----BEGIN PUBLIC KEY-----
            $publicKey
            -----END PUBLIC KEY-----
            """.trimIndent()

        val casperPrivateKey =
            """
            -----BEGIN PRIVATE KEY-----
            $privateKey
            -----END PRIVATE KEY-----
            """.trimIndent()

        return casperSdk.loadKeyPair(
            casperPublicKey.byteInputStream(),
            casperPrivateKey.byteInputStream()
        )
    }
}
