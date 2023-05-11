package sbp.service

import arrow.core.getOrHandle
import com.noumenadigital.npl.api.generated.sbp.ParticipantFacade
import com.noumenadigital.platform.engine.client.EngineClientApi
import keycloak.uuid
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import sbp.model.ParticipantDetailsWithUUID
import sbp.model.participantDetailsLens
import seed.security.ForwardAuthorization

class LoggedInUserHandler(
    private val engineClient: EngineClientApi,
    private val authorizationProvider: ForwardAuthorization,
) {

    fun getLoggedInUserData(): HttpHandler {
        return { req ->
            val auth = authorizationProvider.forward(req)
            val party = authorizationProvider.party(req)
            val loggedInUserId = uuid(party)
            val userProtocol = engineClient.getProtocolStateById(loggedInUserId, auth).getOrHandle { throw it }

            val participantFacade = ParticipantFacade(userProtocol)
            val participantDetails = ParticipantDetailsWithUUID(loggedInUserId, participantFacade)

            Response(Status.OK).with(participantDetailsLens of participantDetails)
        }
    }
}
