package sbp.service

import com.noumenadigital.codegen.ProtocolReference
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi
import keycloak.SbpKeycloakClient
import mu.KotlinLogging
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import sbp.model.ParticipantDetailsWithUUID
import sbp.model.participantCreateModelLens
import sbp.model.participantDetailsLens
import util.createKeycloakUser
import util.createUserProtocol

private val logger = KotlinLogging.logger {}

class ParticipantHandler(
    private val engineClient: EngineClientApi,
    private val sbp: ProtocolReference,
    private val keycloakClient: SbpKeycloakClient,
    private val sbpSystemKeycloakUser: AuthorizationProvider,
    private val adminKeycloakUser: AuthorizationProvider,
) {

    fun create(): HttpHandler {
        return { req ->
            val userRequest = participantCreateModelLens.extract(req)

            val userProtocol = createUserProtocol(
                sbp = sbp,
                engineClient = engineClient,
                participantData = userRequest,
                sbpUser = sbpSystemKeycloakUser
            )

            createKeycloakUser(
                keycloakClient = keycloakClient,
                username = userRequest.name,
                password = userRequest.password,
                participantUUID = userProtocol.id,
                adminKeycloakUser = adminKeycloakUser
            )

            logger.info { "User created with the [username: ${userRequest.name}, uuid: ${userProtocol.id}]" }

            val participantDetails = ParticipantDetailsWithUUID(userProtocol.id, userRequest)

            Response(Status.OK).with(participantDetailsLens of participantDetails)
        }
    }
}
