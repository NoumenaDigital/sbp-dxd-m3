package sbp.controllers

import com.noumenadigital.platform.engine.client.EngineClientApi
import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import sbp.service.LoggedInUserHandler
import seed.security.ForwardAuthorization

fun loggedInUserRoutes(engineClient: EngineClientApi, forwardAuthorization: ForwardAuthorization): RoutingHttpHandler {
    val loggedInUserHandler = LoggedInUserHandler(engineClient, forwardAuthorization)

    return routes(
        "sbp/api/v1/me" bind Method.GET to loggedInUserHandler.getLoggedInUserData()
    )
}
