package sbp.service

import arrow.core.getOrHandle
import com.noumenadigital.codegen.ProtocolReference
import com.noumenadigital.npl.api.generated.sbp.InvoiceFacade
import com.noumenadigital.npl.api.generated.sbp.OfferFacade
import com.noumenadigital.npl.api.generated.sbp.ParticipantProxy
import com.noumenadigital.npl.api.generated.sbp.SBPProxy
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi
import com.noumenadigital.platform.engine.values.ClientProtocolReferenceValue
import keycloak.uuid
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.uuid
import sbp.model.OfferOriginalInvoiceId
import sbp.model.OfferResponse
import sbp.model.OffersResponse
import sbp.model.offerOriginalInvoiceIdLens
import sbp.model.offerPriceRequestLens
import sbp.model.offerResponseLens
import sbp.model.offersResponseLens
import seed.security.ForwardAuthorization

internal val pathOfferId = Path.uuid().of("offer_uuid")

class OfferHandler(
    private val engineClient: EngineClientApi,
    private val sbp: ProtocolReference,
    private val forwardAuth: ForwardAuthorization,
    private val sbpUser: AuthorizationProvider,
) {
    private val sbpProxy = SBPProxy(engineClient)
    private val participantProxy =
        ParticipantProxy(engineClient)

    fun getOpenOffers(): HttpHandler {
        return {
            val offerRefs = sbpProxy.getOpenOffers(sbp.id, sbpUser).getOrHandle { throw it }.result

            val offers = offerResponses(offerRefs)

            Response(Status.OK).with(offersResponseLens of OffersResponse(offers))
        }
    }

    fun getById(): HttpHandler {
        return { req ->
            val auth = forwardAuth.forward(req)
            val offerId = pathOfferId(req)

            val offerState = engineClient.getProtocolStateById(offerId, auth).getOrHandle { throw it }
            val offerFacade = OfferFacade(offerState)

            val invoiceId = offerFacade.fields.originalInvoice.id
            val invoiceState = engineClient.getProtocolStateById(invoiceId, sbpUser).getOrHandle { throw it }
            val invoiceFacade = InvoiceFacade(invoiceState)

            val offerResponse = OfferResponse(offerFacade, invoiceFacade)

            Response(Status.OK).with(offerResponseLens of offerResponse)
        }
    }

    fun getMyIssuedOffers(): HttpHandler {
        return { req ->
            val auth = forwardAuth.forward(req)
            val party = forwardAuth.party(req)
            val participantId = uuid(party)

            val offerRefs = participantProxy.returnMyIssuedOffers(participantId, auth).getOrHandle { throw it }.result

            val offers = offerResponses(offerRefs)

            Response(Status.OK).with(offersResponseLens of OffersResponse(offers))
        }
    }

    fun getMyBoughtOffers(): HttpHandler {
        return { req ->
            val auth = forwardAuth.forward(req)
            val party = forwardAuth.party(req)
            val buyerId = uuid(party)

            val offerRefs = participantProxy.returnMyBoughtOffers(buyerId, auth).getOrHandle { throw it }.result

            val offers = offerResponses(offerRefs)

            Response(Status.OK).with(offersResponseLens of OffersResponse(offers))
        }
    }

    fun buyOffer(): HttpHandler {
        return { req ->
            val auth = forwardAuth.forward(req)
            val party = forwardAuth.party(req)
            val buyerId = uuid(party)
            val offerId = pathOfferId(req)

            val offerState = engineClient.getProtocolStateById(offerId, sbpUser).getOrHandle { throw it }

            val offerRef = ProtocolReference(offerId, OfferFacade.prototypeId)
            participantProxy.buyOffer(buyerId, offerRef, auth).getOrHandle { throw it }.result

            val originalInvoiceId = (offerState.fields["originalInvoice"] as ClientProtocolReferenceValue).value

            Response(Status.OK).with(offerOriginalInvoiceIdLens of OfferOriginalInvoiceId(originalInvoiceId))
        }
    }

    fun updateOfferPrice(): HttpHandler {
        return { req ->
            val auth = forwardAuth.forward(req)
            val party = forwardAuth.party(req)
            val participantId = uuid(party)
            val offerId = pathOfferId(req)
            val offerPriceRequest = offerPriceRequestLens(req)

            val offerRef = ProtocolReference(offerId, OfferFacade.prototypeId)
            val newOfferAmount = offerPriceRequest.offerPrice.amount

            participantProxy.updateOfferPrice(participantId, offerRef, newOfferAmount, auth).getOrHandle { throw it }

            Response(Status.NO_CONTENT)
        }
    }

    private fun offerResponses(offerRefs: Set<ProtocolReference>): List<OfferResponse> {
        val offers = offerRefs
            .map { offer ->
                val offerState = engineClient.getProtocolStateById(offer.id, sbpUser).getOrHandle { throw it }
                val offerFacade = OfferFacade(offerState)

                val invoiceId = offerFacade.fields.originalInvoice.id
                val invoiceState = engineClient.getProtocolStateById(invoiceId, sbpUser).getOrHandle { throw it }
                val invoiceFacade = InvoiceFacade(invoiceState)

                OfferResponse(offerFacade, invoiceFacade)
            }
        return offers
    }
}
