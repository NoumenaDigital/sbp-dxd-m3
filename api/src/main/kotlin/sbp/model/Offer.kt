package sbp.model

import com.noumenadigital.npl.api.generated.sbp.InvoiceFacade
import com.noumenadigital.npl.api.generated.sbp.OfferFacade
import config.JSON.auto
import org.http4k.core.Body
import java.time.ZonedDateTime
import java.util.UUID

data class OffersResponse(
    val offers: List<OfferResponse>,
)

data class OfferResponse(
    val uuid: UUID,
    val supplierUUID: UUID,
    val invoiceNumber: String,
    val price: Amount,
    val amount: Amount,
    val supplierData: PaymentDetails,
    val debtorData: CustomerDetails,
    val paymentDeadline: ZonedDateTime?,
    val blockchainRef: String?,
    val dateListed: ZonedDateTime,
    val state: String,
) {
    constructor(offerFacade: OfferFacade, invoiceFacade: InvoiceFacade) : this(
        uuid = offerFacade.id.id,
        supplierUUID = offerFacade.fields.supplier.id,
        invoiceNumber = invoiceFacade.fields.invoiceNumber,
        price = Amount(offerFacade.fields.amount),
        amount = Amount(invoiceFacade.fields.amount),
        supplierData = PaymentDetails(invoiceFacade.fields.creditorData),
        debtorData = CustomerDetails(invoiceFacade.fields.debtorData),
        paymentDeadline = offerFacade.fields.paymentDeadline,
        blockchainRef = invoiceFacade.fields.blockchainRef,
        dateListed = offerFacade.fields.dateListed,
        state = offerFacade.activeState.name
    )
}

data class OfferPrice(
    val offerPrice: Amount,
)

data class OfferOriginalInvoiceId(val originalInvoiceId: UUID)

val offerResponseLens = Body.auto<OfferResponse>().toLens()
val offersResponseLens = Body.auto<OffersResponse>().toLens()
val offerPriceRequestLens = Body.auto<OfferPrice>().toLens()
val offerOriginalInvoiceIdLens = Body.auto<OfferOriginalInvoiceId>().toLens()
