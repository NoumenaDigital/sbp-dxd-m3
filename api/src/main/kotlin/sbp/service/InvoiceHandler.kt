package sbp.service

import arrow.core.getOrHandle
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import com.noumenadigital.codegen.ProtocolReference
import com.noumenadigital.npl.api.generated.sbp.InvoiceFacade
import com.noumenadigital.npl.api.generated.sbp.InvoiceProxy
import com.noumenadigital.npl.api.generated.sbp.MilestoneFacade
import com.noumenadigital.npl.api.generated.sbp.ParticipantProxy
import com.noumenadigital.npl.api.generated.sbp.SBPProxy
import com.noumenadigital.npl.api.generated.support.CustomerDetailsFacade
import com.noumenadigital.npl.api.generated.support.PaymentDetailsFacade
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi
import config.ISbpdxdConfiguration
import config.XML
import keycloak.uuid
import org.apache.commons.codec.binary.Base64.isBase64
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.uuid
import sbp.model.CustomerDetails
import sbp.model.ErrorResponse
import sbp.model.InvoiceDetails
import sbp.model.InvoiceResponse
import sbp.model.InvoiceVerifiedResponse
import sbp.model.InvoiceXml
import sbp.model.InvoicesResponse
import sbp.model.MilestoneResponse
import sbp.model.PaymentDetails
import sbp.model.ValidationError
import sbp.model.ValidationErrors
import sbp.model.ValidationException
import sbp.model.invoiceRequestLens
import sbp.model.invoiceResponseLens
import sbp.model.invoiceVerifyRequestLens
import sbp.model.invoiceVerifyResponseLens
import sbp.model.invoicesResponseLens
import sbp.model.toMilestoneDetails
import sbp.model.verifiedErrorResponse
import seed.security.ForwardAuthorization
import sign.OnChainClient
import java.time.ZonedDateTime
import java.util.Base64

internal val pathInvoiceId = Path.uuid().of("invoice_uuid")

class InvoiceHandler(
    private val config: ISbpdxdConfiguration,
    private val engineClient: EngineClientApi,
    private val sbp: ProtocolReference,
    private val onChainClient: OnChainClient,
    private val forwardAuth: ForwardAuthorization,
    private val sbpUser: AuthorizationProvider,
) {
    private val participantProxy = ParticipantProxy(engineClient)
    private val invoiceProxy = InvoiceProxy(engineClient)
    private val sbpProxy = SBPProxy(engineClient)

    fun create(): HttpHandler {
        return { req ->
            val auth = forwardAuth.forward(req)
            val invoiceRequest = invoiceRequestLens.extract(req)

            val party = forwardAuth.party(req)
            val participantId = uuid(party)

            val invoiceRef = participantProxy.createNewInvoice(
                protocolId = participantId,
                invoiceNumber = invoiceRequest.invoiceNumber,
                amount = invoiceRequest.amount.toFacade(),
                creditorData = invoiceRequest.creditorData.toFacade(),
                debtorData = invoiceRequest.debtorData.toFacade(),
                deadLine = invoiceRequest.deadline,
                issueDate = invoiceRequest.issueDateTime,
                freeTextDescription = invoiceRequest.freeTextDescription,
                toBeTraded = invoiceRequest.toBeTraded,
                offerAmount = invoiceRequest.offerAmount?.toFacade(),
                authorizationProvider = auth
            ).getOrHandle { throw it }.result

            val invoiceId = invoiceRef.id
            if (config.includeIpfs) {
                val ipfsHash = onChainClient.create(invoiceRequest)
                invoiceProxy.setBlockChainRef(invoiceId, ipfsHash, sbpUser).getOrHandle { throw it }
            }

            invoiceProxy.finaliseInvoice(invoiceId, sbpUser).getOrHandle { throw it }

            val invoiceProtocol = engineClient.getProtocolStateById(invoiceId, auth).getOrHandle { throw it }

            val invoiceFacade = InvoiceFacade(invoiceProtocol)
            val response = invoiceFacade.toInvoiceResponse(auth)

            Response(Status.CREATED).with(invoiceResponseLens of response)
        }
    }

    fun issuedInvoices(): HttpHandler {
        return { req ->
            val auth = forwardAuth.forward(req)
            val party = forwardAuth.party(req)
            val participantId = uuid(party)

            val invoiceRefs =
                participantProxy.returnMyIssuedInvoices(participantId, auth).getOrHandle { throw it }.result

            val invoicesResult = invoiceRefs
                .map { invoiceRef ->
                    val invoiceProtocol =
                        engineClient.getProtocolStateById(invoiceRef.id, auth).getOrHandle { throw it }

                    val invoiceFacade = InvoiceFacade(invoiceProtocol)
                    invoiceFacade.toInvoiceResponse(auth)
                }

            Response(Status.OK).with(invoicesResponseLens of InvoicesResponse(invoicesResult))
        }
    }

    fun getById(): HttpHandler {
        return { req ->
            val invoiceId = pathInvoiceId(req)
            val auth = forwardAuth.forward(req)

            val invoiceProtocol = engineClient.getProtocolStateById(invoiceId, auth).getOrHandle { throw it }

            val invoiceFacade = InvoiceFacade(invoiceProtocol)
            val response = invoiceFacade.toInvoiceResponse(auth)

            Response(Status.OK).with(invoiceResponseLens of response)
        }
    }

    fun verify(): HttpHandler {
        return { req ->
            val invoiceFile = invoiceVerifyRequestLens.extract(req)

            if (!isBase64(invoiceFile.base64EncodedFile)) {
                val error =
                    ValidationError("base64EncodedFile", invoiceFile.base64EncodedFile, "String must be bas64 encoded.")
                val errors = ValidationErrors(mutableListOf(error))

                throw ValidationException(errors = errors)
            }

            if (!invoiceFile.type.equals("xml", ignoreCase = true)) {
                val error = ValidationError("type", invoiceFile.type, "Type must be xml.")
                val errors = ValidationErrors(mutableListOf(error))

                throw ValidationException(errors = errors)
            }

            try {
                val invoiceXml = XML.mapper.readValue(
                    Base64.getDecoder().decode(invoiceFile.base64EncodedFile),
                    InvoiceXml::class.java
                )

                val creditorData = invoiceXml.accountingSupplierParty.party.let {
                    PaymentDetailsFacade(
                        it.iban!!,
                        it.partyName.name!!,
                        it.partyName.address!!
                    )
                }

                val debtorData = invoiceXml.accountingCustomerParty.party.partyName.let {
                    CustomerDetailsFacade(it.name!!, it.address!!)
                }

                val invoiceDetails = InvoiceDetails(
                    invoiceNumber = invoiceXml.id,
                    amount = invoiceXml.amount,
                    creditorData = PaymentDetails(creditorData),
                    debtorData = CustomerDetails(debtorData),
                    issueDate = ZonedDateTime.from(invoiceXml.issueDate),
                    deadLine = ZonedDateTime.from(invoiceXml.deadline),
                    description = invoiceXml.freeTextDescription,
                    bcId = null
                )

                val isVerified = sbpProxy.verifyAuthenticity(
                    protocolId = sbp.id,
                    invoiceDetails = invoiceDetails.toFacade(),
                    authorizationProvider = sbpUser
                ).getOrHandle { throw it }.result

                Response(Status.OK).with(invoiceVerifyResponseLens of InvoiceVerifiedResponse(isVerified))
            } catch (e: IllegalArgumentException) {
                Response(Status.BAD_REQUEST)
                    .with(verifiedErrorResponse of ErrorResponse("Requested XML has invalid format"))
            } catch (e: ValueInstantiationException) {
                Response(Status.BAD_REQUEST)
                    .with(verifiedErrorResponse of ErrorResponse("Xml file has some missing fields"))
            }
        }
    }

    private fun InvoiceFacade.toInvoiceResponse(auth: AuthorizationProvider): InvoiceResponse {
        val milestoneUUID = fields.milestone?.id
        val milestoneResponse = if (milestoneUUID != null) {
            val milestoneFacade =
                MilestoneFacade(engineClient.getProtocolStateById(milestoneUUID, auth).getOrHandle { throw it })
            MilestoneResponse(
                milestoneFacade.id.id,
                milestoneFacade.toMilestoneDetails(engineClient, auth)
            )
        } else {
            null
        }
        return InvoiceResponse(this, milestoneResponse)
    }
}
