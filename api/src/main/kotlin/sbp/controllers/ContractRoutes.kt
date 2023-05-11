package sbp.controllers

import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi
import config.ISbpdxdConfiguration
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import sbp.service.ContractHandler
import seed.filter.loginRequired
import seed.security.ForwardAuthorization
import sign.OnChainClient

fun contractRoutes(
    config: ISbpdxdConfiguration,
    engineClient: EngineClientApi,
    onChainClient: OnChainClient,
    forwardAuth: ForwardAuthorization,
    sbpUser: AuthorizationProvider,
): RoutingHttpHandler {
    val contractHandler = ContractHandler(
        config,
        engineClient,
        onChainClient,
        forwardAuth,
        sbpUser
    )

    return routes(
        "/sbp/api/v1" bind routes(
            "/contracts" bind Method.POST to loginRequired(config).then(contractHandler.create()),
            "/contracts" bind Method.GET to loginRequired(config).then(contractHandler.getMyContracts()),
            "/contracts/{contract_uuid}" bind Method.GET to contractHandler.getById(),
            "/contracts/{contract_uuid}/sign" bind Method.POST to contractHandler.signContract(),
            "/contracts/{contract_uuid}/milestones/{milestone_uuid}" bind Method.GET to contractHandler.getMilestoneForContractById(),
            "/contracts/{contract_uuid}/milestones/{milestone_uuid}/close" bind Method.POST to contractHandler.closeMilestone(),
            "/contracts/{contract_uuid}/milestones/{milestone_uuid}/offerPrice" bind Method.PUT to contractHandler.updateOfferPrice()
        )
    )
}
