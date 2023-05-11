package utils

data class Config(
    val includeIpfs: Boolean = (System.getenv("INCLUDE_IPFS") ?: "true").toBoolean(),
    val disableContracts: Boolean = (System.getenv("DISABLE_CONTRACT_ENDPOINTS") ?: "false").toBoolean()
)
