package sbp.controllers

import com.noumenadigital.codegen.ProtocolReference
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi
import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import sbp.service.OfferHandler
import seed.security.ForwardAuthorization

fun offerRoutes(
    engineClient: EngineClientApi,
    sbp: ProtocolReference,
    forwardAuthorization: ForwardAuthorization,
    sbpUser: AuthorizationProvider,
): RoutingHttpHandler {
    val offerHandler = OfferHandler(
        engineClient,
        sbp,
        forwardAuthorization,
        sbpUser
    )

    return routes(
        "/sbp/api/v1" bind routes(
            "/offers/marketplace" bind Method.GET to offerHandler.getOpenOffers(),
            "/offers/portfolio" bind Method.GET to offerHandler.getMyBoughtOffers(),
            "/offers/issuedOffers" bind Method.GET to offerHandler.getMyIssuedOffers(),
            "/offers/{offer_uuid}" bind Method.GET to offerHandler.getById(),
            "/offers/{offer_uuid}/buy" bind Method.POST to offerHandler.buyOffer(),
            "/offers/{offer_uuid}/offerPrice" bind Method.PATCH to offerHandler.updateOfferPrice()
        )
    )
}
