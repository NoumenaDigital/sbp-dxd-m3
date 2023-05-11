package sbp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.noumenadigital.npl.api.generated.sbp.ContractFacade
import com.noumenadigital.npl.api.generated.sbp.MilestoneFacade
import com.noumenadigital.npl.api.generated.support.MilestoneDetailsFacade
import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import java.time.Clock
import java.time.Period
import java.time.ZonedDateTime
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContractRequest(
    val id: String,
    val ccy: String,
    val name: String,
    val creationDate: ZonedDateTime,
    val goodsValue: Amount,
    val customerDetails: CustomerDetails,
    val expectedDeliveryDate: ZonedDateTime,
    val milestones: List<MilestoneRequest>,
)

data class ContractsResponse(
    val contracts: List<ContractResponse>,
)

data class ContractResponse(
    val uuid: UUID,
    val status: String,
    val id: String,
    val ccy: String,
    val name: String,
    val creationDate: ZonedDateTime,
    val goodsValue: Amount,
    val supplierDetails: Participant,
    val customerDetails: CustomerDetails,
    val expectedDeliveryDate: ZonedDateTime,
    val signDate: ZonedDateTime?,
    val milestones: List<MilestoneResponse>,
    val completionPercentage: Int
) {
    constructor(
        facade: ContractFacade,
        supplierDetails: Participant,
        milestones: List<MilestoneResponse>,
        completionPercentage: Int
    ) : this(
        uuid = facade.id.id,
        status = facade.state.currentState!!,
        id = facade.fields.id,
        ccy = facade.fields.contractCCY.toString(),
        name = facade.fields.name,
        creationDate = facade.fields.creationDate,
        goodsValue = Amount(facade.fields.goodsValue),
        supplierDetails = supplierDetails,
        customerDetails = CustomerDetails(facade.fields.customerDetails),
        expectedDeliveryDate = facade.fields.expectedDeliveryDate,
        signDate = facade.fields.signDate,
        milestones = milestones,
        completionPercentage = completionPercentage
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MilestoneRequest(
    val name: String,
    val description: String,
    val amount: Amount,
    val toBeTraded: Boolean?,
    val offerAmount: Amount?,
    val paymentPeriod: String,
) {
    fun toFacade(clock: Clock = Clock.systemUTC()) = MilestoneDetailsFacade(
        name = name,
        amount = amount.toFacade(),
        description = description,
        timeToPay = Period.parse(paymentPeriod),
        invoiceToTrade = toBeTraded ?: false,
        offerAmount = offerAmount?.toFacade(),
        dateCreated = ZonedDateTime.now(clock)
    )
}

data class ExpandedMilestoneResponse(
    val uuid: UUID,
    val milestoneDetails: MilestoneDetails,
    val supplierDetails: Participant,
    val customerDetails: CustomerDetails,
    val contractDetails: ReducedContractDetails,
)

data class MilestoneResponse(
    val uuid: UUID,
    val details: MilestoneDetails,
)

data class MilestoneDetails(
    val status: String,
    val name: String,
    val description: String,
    val amount: Amount?,
    val toBeTraded: Boolean,
    val offerAmount: Amount?,
    val dateCreated: ZonedDateTime,
    val dateCompleted: ZonedDateTime?,
    val expectedPaymentDate: ZonedDateTime?,
    val blockchainReference: String?,
    val timeToPay: String,
    val originalInvoiceUUID: UUID?,
) {
    constructor(facade: MilestoneFacade, blockchainReference: String?) : this(
        status = facade.state.currentState!!,
        name = facade.fields.name,
        description = facade.fields.description,
        amount = Amount(facade.fields.amount),
        toBeTraded = facade.fields.invoiceToTrade,
        offerAmount = facade.fields.offerAmount?.let { Amount(it) },
        dateCreated = facade.fields.dateCreated,
        dateCompleted = facade.fields.dateCompleted,
        expectedPaymentDate = facade.fields.originalPaymentDeadline,
        blockchainReference = blockchainReference,
        timeToPay = facade.fields.timeToPay.toString(),
        originalInvoiceUUID = facade.fields.originalInvoice?.id
    )
}

data class ReducedContractDetails(
    val uuid: UUID,
    val name: String,
) {
    constructor(facade: ContractFacade) : this(facade.id.id, facade.fields.name)
}

val contractRequestLens = Body.auto<ContractRequest>().toLens()
val contractsResponseLens = Body.auto<ContractsResponse>().toLens()
val contractResponseLens = Body.auto<ContractResponse>().toLens()
val milestoneResponseLens = Body.auto<MilestoneResponse>().toLens()
val expandedMilestoneResponseLens = Body.auto<ExpandedMilestoneResponse>().toLens()
