package sbp.controllers

import com.noumenadigital.codegen.ProtocolReference
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi
import config.ISbpdxdConfiguration
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import sbp.service.InvoiceHandler
import seed.filter.loginRequired
import seed.security.ForwardAuthorization
import sign.OnChainClient

fun invoiceRoutes(
    config: ISbpdxdConfiguration,
    engineClient: EngineClientApi,
    sbp: ProtocolReference,
    onChainClient: OnChainClient,
    forwardAuth: ForwardAuthorization,
    sbpUser: AuthorizationProvider,
): RoutingHttpHandler {
    val invoiceHandler = InvoiceHandler(
        config,
        engineClient,
        sbp,
        onChainClient,
        forwardAuth,
        sbpUser
    )

    return routes(
        "/sbp/api/v1" bind routes(
            "/invoices" bind Method.POST to loginRequired(config).then(invoiceHandler.create()),
            "/invoices" bind Method.GET to loginRequired(config).then(invoiceHandler.issuedInvoices()),
            "/invoices/verify" bind Method.POST to invoiceHandler.verify(),
            "/invoices/{invoice_uuid}" bind Method.GET to invoiceHandler.getById()
        )
    )
}
