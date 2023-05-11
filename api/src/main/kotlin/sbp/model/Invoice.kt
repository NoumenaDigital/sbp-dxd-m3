package sbp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.noumenadigital.npl.api.generated.sbp.InvoiceFacade
import com.noumenadigital.npl.api.generated.support.CustomerDetailsFacade
import com.noumenadigital.npl.api.generated.support.InvoiceDetailsFacade
import com.noumenadigital.npl.api.generated.support.PaymentDetailsFacade
import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import java.time.ZonedDateTime
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class InvoiceRequest(
    val invoiceNumber: String,
    val amount: Amount,
    val creditorData: PaymentDetails,
    val debtorData: CustomerDetails,
    val issueDateTime: ZonedDateTime,
    val deadline: ZonedDateTime,
    val freeTextDescription: String,
    val toBeTraded: Boolean,
    val offerAmount: Amount?,
)

data class InvoiceResponse(
    val uuid: UUID,
    val milestone: MilestoneResponse?,
    val invoiceNumber: String,
    val amount: Amount,
    val creditorData: PaymentDetails,
    val debtorData: CustomerDetails,
    val issueDateTime: ZonedDateTime,
    val deadline: ZonedDateTime,
    val freeTextDescription: String,
    val toBeTraded: Boolean,
    val offerAmount: Amount?,
    val blockchainReference: String?,
    val invoiceStatus: InvoiceFacade.StatesEnum,
) {
    constructor(
        facade: InvoiceFacade,
        milestone: MilestoneResponse?,
    ) : this(
        uuid = facade.id.id,
        milestone = milestone,
        invoiceNumber = facade.fields.invoiceNumber,
        amount = Amount(facade.fields.amount),
        creditorData = PaymentDetails(facade.fields.creditorData),
        debtorData = CustomerDetails(facade.fields.debtorData),
        deadline = facade.fields.deadline,
        issueDateTime = facade.fields.issueDate,
        freeTextDescription = facade.fields.freeTextDescription,
        toBeTraded = facade.fields.toBeTraded,
        offerAmount = facade.fields.offerAmount?.let { Amount(it) },
        blockchainReference = facade.fields.blockchainRef,
        invoiceStatus = facade.activeState
    )
}

data class InvoiceDetails(
    val invoiceNumber: String,
    val amount: Amount,
    val creditorData: PaymentDetails,
    val debtorData: CustomerDetails,
    val deadLine: ZonedDateTime,
    val issueDate: ZonedDateTime,
    val description: String,
    val bcId: String?,
) {
    fun toFacade() = InvoiceDetailsFacade(
        invoiceNumber,
        amount.toFacade(),
        creditorData.toFacade(),
        debtorData.toFacade(),
        deadLine,
        issueDate,
        description,
        bcId
    )
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentDetails(
    val iban: String,
    val name: String,
    val address: String
) {
    constructor(facade: PaymentDetailsFacade) : this(
        facade.iban,
        facade.name,
        facade.address
    )

    fun toFacade() = PaymentDetailsFacade(
        iban,
        name,
        address
    )
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CustomerDetails(
    val name: String,
    val address: String,
) {
    constructor(facade: CustomerDetailsFacade) : this(facade.name, facade.address)

    fun toFacade() = CustomerDetailsFacade(name, address)
}

data class InvoicesResponse(
    val invoices: List<InvoiceResponse>,
)

data class InvoiceVerifyRequest(
    val base64EncodedFile: String,
    val type: String,
)

data class InvoiceVerifiedResponse(
    val verified: Boolean,
)

val invoiceRequestLens = Body.auto<InvoiceRequest>().toLens()
val invoicesResponseLens = Body.auto<InvoicesResponse>().toLens()
val invoiceResponseLens = Body.auto<InvoiceResponse>().toLens()
val invoiceVerifyResponseLens = Body.auto<InvoiceVerifiedResponse>().toLens()
val invoiceVerifyRequestLens = Body.auto<InvoiceVerifyRequest>().toLens()
