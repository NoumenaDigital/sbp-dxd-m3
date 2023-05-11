package sign

import config.ISbpdxdConfiguration
import config.SnakeCaseJsonConfiguration
import ipfs.IpfsStore
import org.http4k.core.ContentType
import org.http4k.lens.MultipartFormFile
import sbp.model.InvoiceRequest
import java.util.Base64

fun interface OnChainClient {
    fun create(invoiceRequest: InvoiceRequest): String
}

class SignedIpfsOnChainClient(
    private val ipfsClient: IpfsStore,
    private val signer: Signer,
    private val config: ISbpdxdConfiguration,
) : OnChainClient {
    override fun create(invoiceRequest: InvoiceRequest): String {
        val invoiceDataAsString = SnakeCaseJsonConfiguration.mapper.writeValueAsString(invoiceRequest)
        val base64EncodedInvoice = Base64.getEncoder().encodeToString(invoiceDataAsString.toByteArray())
        val onChainInvoice = OnChainInvoice(
            base64EncodedData = base64EncodedInvoice,
            signedBy = "did:${config.didMethod}:${config.publicKeySbp}",
            signature = signer.sign(base64EncodedInvoice)
        )
        val invoiceAsFile = MultipartFormFile(
            filename = "signed_invoice.json",
            contentType = ContentType.APPLICATION_JSON,
            content = SnakeCaseJsonConfiguration.mapper.writeValueAsString(onChainInvoice).byteInputStream()
        )
        return ipfsClient.store(invoiceAsFile)
    }
}

data class OnChainInvoice(
    val base64EncodedData: String,
    val signedBy: String,
    val signature: String,
)
