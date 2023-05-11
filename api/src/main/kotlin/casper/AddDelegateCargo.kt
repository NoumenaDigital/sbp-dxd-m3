package casper

data class AddDelegateCargo(
    val privateKeyIdentity: String,
    val publicKeyIdentity: String,
    val contractNamedKey: String = "did_registry_sbp_v1_contract_hash",
    val gas: String = "3000000000",
    val didOwnerAccountHash: String,
    val delegateType: String = "sbp-delegates",
    val delegateAccountHash: String,
    val validity: String = "9999999999",
)
