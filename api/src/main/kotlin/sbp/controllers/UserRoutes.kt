package sbp.controllers

import com.noumenadigital.codegen.ProtocolReference
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi
import keycloak.SbpKeycloakClient
import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import sbp.service.ParticipantHandler

fun userRoutes(
    engineClient: EngineClientApi,
    sbp: ProtocolReference,
    keycloakClient: SbpKeycloakClient,
    sbpSystemKeycloakUser: AuthorizationProvider,
    adminKeycloakUser: AuthorizationProvider,
): RoutingHttpHandler {

    val participantHandler = ParticipantHandler(
        engineClient,
        sbp,
        keycloakClient,
        sbpSystemKeycloakUser,
        adminKeycloakUser
    )

    return routes(
        // for now, it's client's role to keep the data integrity intact, like non-duplicate iban etc.
        "sbp/api/v1/user" bind Method.POST to participantHandler.create()
    )
}
