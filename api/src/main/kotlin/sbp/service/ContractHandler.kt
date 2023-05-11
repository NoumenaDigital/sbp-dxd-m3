package sbp.service

import arrow.core.getOrHandle
import com.noumenadigital.codegen.ProtocolReference
import com.noumenadigital.npl.api.generated.sbp.ContractFacade
import com.noumenadigital.npl.api.generated.sbp.ContractProxy
import com.noumenadigital.npl.api.generated.sbp.InvoiceProxy
import com.noumenadigital.npl.api.generated.sbp.MilestoneFacade
import com.noumenadigital.npl.api.generated.sbp.MilestoneProxy
import com.noumenadigital.npl.api.generated.sbp.ParticipantFacade
import com.noumenadigital.npl.api.generated.sbp.ParticipantProxy
import com.noumenadigital.npl.api.generated.support.CCYEnum
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi
import config.ISbpdxdConfiguration
import keycloak.uuid
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.uuid
import sbp.model.Amount
import sbp.model.ContractResponse
import sbp.model.ContractsResponse
import sbp.model.CustomerDetails
import sbp.model.ExpandedMilestoneResponse
import sbp.model.InvoiceRequest
import sbp.model.MilestoneResponse
import sbp.model.Participant
import sbp.model.PaymentDetails
import sbp.model.ReducedContractDetails
import sbp.model.ValidationError
import sbp.model.ValidationErrors
import sbp.model.ValidationException
import sbp.model.contractRequestLens
import sbp.model.contractResponseLens
import sbp.model.contractsResponseLens
import sbp.model.expandedMilestoneResponseLens
import sbp.model.isValidPeriod
import sbp.model.milestoneResponseLens
import sbp.model.offerPriceRequestLens
import sbp.model.toMilestoneDetails
import seed.security.ForwardAuthorization
import sign.OnChainClient
import java.time.Clock
import java.time.ZonedDateTime

internal val pathMilestoneId = Path.uuid().of("milestone_uuid")
internal val pathContractId = Path.uuid().of("contract_uuid")

class ContractHandler(
    private val config: ISbpdxdConfiguration,
    private val engineClient: EngineClientApi,
    private val onChainClient: OnChainClient,
    private val forwardAuth: ForwardAuthorization,
    private val sbpUser: AuthorizationProvider,
) {
    private val participantProxy = ParticipantProxy(engineClient)
    private val contractProxy = ContractProxy(engineClient)
    private val milestoneProxy = MilestoneProxy(engineClient)
    private val invoiceProxy = InvoiceProxy(engineClient)

    fun create(): HttpHandler {
        return { req ->
            val auth = forwardAuth.forward(req)
            val contractRequest = contractRequestLens.extract(req)

            for (milestone in contractRequest.milestones) {
                if (!isValidPeriod(milestone.paymentPeriod)) {
                    val error = ValidationError("paymentPeriod", milestone.paymentPeriod, "Not a valid period format.")
                    val errors = ValidationErrors(mutableListOf(error))

                    throw ValidationException(errors = errors)
                }
            }

            val party = forwardAuth.party(req)
            val participantId = uuid(party)

            val contractResponse = contractRequest.run {
                participantProxy.createContract(
                    protocolId = participantId,
                    id = id,
                    ccy = CCYEnum.valueOf(ccy),
                    contractName = name,
                    creationDate = creationDate,
                    goodsValue = goodsValue.toFacade(),
                    milestones = milestones.map { it.toFacade() },
                    customerDetails = customerDetails.toFacade(),
                    expectedDeliveryDate = expectedDeliveryDate,
                    authorizationProvider = auth
                )
            }

            val id = contractResponse.getOrHandle { throw it }.result
            val contractProtocol = engineClient.getProtocolStateById(id.id, auth).getOrHandle { throw it }
            val contractFacade = ContractFacade(contractProtocol)

            val response = contractFacade.toContractResponse(auth)

            Response(Status.CREATED).with(contractResponseLens of response)
        }
    }

    fun getMyContracts(): HttpHandler {
        return { req ->
            val auth = forwardAuth.forward(req)
            val party = forwardAuth.party(req)
            val participantId = uuid(party)

            val contractsResponse = participantProxy.returnMyContracts(participantId, auth)
            val ids = contractsResponse.getOrHandle { throw it }.result

            val contracts = ids.map { contractId ->
                val contractProtocol = engineClient.getProtocolStateById(contractId.id, auth).getOrHandle { throw it }
                val contractFacade = ContractFacade(contractProtocol)
                contractFacade.toContractResponse(auth)
            }

            Response(Status.OK).with(contractsResponseLens of ContractsResponse(contracts))
        }
    }

    fun getById(): HttpHandler {
        return { req ->
            val contractId = pathContractId(req)
            val auth = forwardAuth.forward(req)

            val contractProtocol = engineClient.getProtocolStateById(contractId, auth).getOrHandle { throw it }
            val contractFacade = ContractFacade(contractProtocol)
            val contractResponse = contractFacade.toContractResponse(auth)

            Response(Status.OK).with(contractResponseLens of contractResponse)
        }
    }

    fun getMilestoneForContractById(): HttpHandler {
        return { req ->
            val milestoneId = pathMilestoneId(req)
            val auth = forwardAuth.forward(req)

            val milestoneProtocol = engineClient.getProtocolStateById(milestoneId, auth).getOrHandle { throw it }
            val milestoneFacade = MilestoneFacade(milestoneProtocol)
            val contractId = milestoneFacade.fields.contract!!.id

            val contractProtocol = engineClient.getProtocolStateById(contractId, auth).getOrHandle { throw it }
            val contractFacade = ContractFacade(contractProtocol)
            val participantId = contractFacade.fields.supplier.id

            val participantProtocol = engineClient.getProtocolStateById(participantId, auth).getOrHandle { throw it }
            val participantFacade = ParticipantFacade(participantProtocol)

            val milestoneResponse = ExpandedMilestoneResponse(
                uuid = milestoneFacade.id.id,
                milestoneDetails = milestoneFacade.toMilestoneDetails(engineClient, auth),
                customerDetails = CustomerDetails(contractFacade.fields.customerDetails),
                supplierDetails = Participant(participantFacade),
                contractDetails = ReducedContractDetails(contractFacade)
            )

            Response(Status.OK).with(expandedMilestoneResponseLens of milestoneResponse)
        }
    }

    fun signContract(): HttpHandler {
        return { req ->
            val contractId = pathContractId(req)
            val auth = forwardAuth.forward(req)

            val now = ZonedDateTime.now(Clock.systemUTC())

            contractProxy.finaliseCreation(contractId, now, auth).getOrHandle { throw it }
            val contractProtocol = engineClient.getProtocolStateById(contractId, auth).getOrHandle { throw it }
            val contractFacade = ContractFacade(contractProtocol)
            val contractResponse = contractFacade.toContractResponse(auth)

            Response(Status.OK).with(contractResponseLens of contractResponse)
        }
    }

    fun closeMilestone(): HttpHandler {
        return { req ->
            val milestoneId = pathMilestoneId(req)
            val auth = forwardAuth.forward(req)

            milestoneProxy.milestoneReached(milestoneId, auth).getOrHandle { throw it }
            val milestoneProtocol = engineClient.getProtocolStateById(milestoneId, auth).getOrHandle { throw it }
            val milestoneFacade = MilestoneFacade(milestoneProtocol)

            val contractId = milestoneFacade.fields.contract!!.id
            val contractProtocol = engineClient.getProtocolStateById(contractId, auth).getOrHandle { throw it }
            val contractFacade = ContractFacade(contractProtocol)

            val participantId = contractFacade.fields.supplier.id
            val participantProtocol = engineClient.getProtocolStateById(participantId, auth).getOrHandle { throw it }
            val participantFacade = ParticipantFacade(participantProtocol)
            val invoiceNumber = "INV_${contractFacade.fields.name}_${milestoneFacade.fields.name}"

            val invoiceProtocol = milestoneProxy.createOriginalInvoice(
                protocolId = milestoneId,
                invoiceNumber = invoiceNumber,
                amount = milestoneFacade.fields.amount,
                creditorData = participantFacade.fields.paymentDetails,
                debtorData = contractFacade.fields.customerDetails,
                deadLine = milestoneFacade.fields.originalPaymentDeadline!!,
                issueDate = milestoneFacade.fields.dateCompleted!!,
                freeTextDescription = milestoneFacade.fields.description,
                authorizationProvider = sbpUser
            ).getOrHandle { throw it }.result

            val invoiceId = invoiceProtocol.id

            if (config.includeIpfs) {
                val invoiceRequest = InvoiceRequest(
                    invoiceNumber = invoiceNumber,
                    amount = Amount(milestoneFacade.fields.amount),
                    creditorData = PaymentDetails(participantFacade.fields.paymentDetails),
                    debtorData = CustomerDetails(contractFacade.fields.customerDetails),
                    deadline = milestoneFacade.fields.originalPaymentDeadline!!,
                    issueDateTime = milestoneFacade.fields.dateCompleted!!,
                    freeTextDescription = milestoneFacade.fields.description,
                    toBeTraded = milestoneFacade.fields.invoiceToTrade,
                    offerAmount = milestoneFacade.fields.offerAmount?.let { Amount(it) }
                )

                val ipfsHash = onChainClient.create(invoiceRequest)
                invoiceProxy.setBlockChainRef(invoiceId, ipfsHash, sbpUser).getOrHandle { throw it }
            }

            invoiceProxy.finaliseInvoice(invoiceId, sbpUser).getOrHandle { throw it }

            val milestoneProtocolUpdated = engineClient.getProtocolStateById(milestoneId, auth).getOrHandle { throw it }
            val milestoneFacadeUpdated = MilestoneFacade(milestoneProtocolUpdated)
            val milestoneDetails = milestoneFacadeUpdated.toMilestoneDetails(engineClient, auth)
            val milestoneResponse = MilestoneResponse(milestoneId, milestoneDetails)

            Response(Status.OK).with(milestoneResponseLens of milestoneResponse)
        }
    }

    fun updateOfferPrice(): HttpHandler {
        return { req ->
            val milestoneId = pathMilestoneId(req)
            val auth = forwardAuth.forward(req)

            val offerPriceRequest = offerPriceRequestLens(req)

            val newOfferAmount = offerPriceRequest.offerPrice.toFacade()

            milestoneProxy.updateOfferAmount(milestoneId, newOfferAmount, auth).getOrHandle { throw it }

            Response(Status.NO_CONTENT)
        }
    }

    private fun ContractFacade.toContractResponse(auth: AuthorizationProvider): ContractResponse {
        val supplierDetails = getSupplierDetails(auth)
        val milestones = getMilestoneResponses(auth)
        val completionPercentage = milestones.getCompletionPercentage()

        return ContractResponse(this, supplierDetails, milestones, completionPercentage)
    }

    private fun ContractFacade.getSupplierDetails(auth: AuthorizationProvider): Participant {
        val participantId = fields.supplier.id
        val participantProtocol = engineClient.getProtocolStateById(participantId, auth).getOrHandle { throw it }
        val participantFacade = ParticipantFacade(participantProtocol)
        return Participant(participantFacade)
    }

    private fun ContractFacade.getMilestoneResponses(auth: AuthorizationProvider): List<MilestoneResponse> {
        val milestoneRefs = fields.milestones
        val allMilestones = milestoneRefs.map { it.toMilestoneFacade(auth) }
        return allMilestones.map { MilestoneResponse(it.id.id, it.toMilestoneDetails(engineClient, auth)) }
    }

    private fun List<MilestoneResponse>.getCompletionPercentage(): Int {
        val completedMilestonesCount = this
            .filter { it.details.status in setOf("reached", "closed") }
            .size

        return if (isNotEmpty()) completedMilestonesCount * 100 / size else 0
    }

    private fun ProtocolReference.toMilestoneFacade(auth: AuthorizationProvider): MilestoneFacade {
        val milestoneProtocol = engineClient.getProtocolStateById(id, auth).getOrHandle { throw it }
        return MilestoneFacade(milestoneProtocol)
    }
}
