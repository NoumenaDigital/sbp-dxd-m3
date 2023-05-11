package util

import arrow.core.getOrHandle
import com.noumenadigital.codegen.Party
import com.noumenadigital.codegen.ProtocolReference
import com.noumenadigital.npl.api.generated.sbp.ParticipantProxy
import com.noumenadigital.npl.api.generated.sbp.SBPFacade
import com.noumenadigital.npl.api.generated.sbp.SBPProxy
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi
import config.JSON
import keycloak.SbpKeycloakClient
import org.http4k.core.Body
import sbp.model.Attributes
import sbp.model.Credentials
import sbp.model.KeycloakUserModel
import sbp.model.Participant
import sbp.service.ProtocolExists
import seed.graphql.GraphQLClient
import java.util.UUID

fun initSbp(
    engineClient: EngineClientApi,
    graphQLClient: GraphQLClient,
    sbpSystemKeycloakUser: AuthorizationProvider,
): ProtocolReference {
    val protocolReference =
        ProtocolExists(graphQLClient, SBPFacade.prototypeId, sbpSystemKeycloakUser).getProtocolReference()
    return if (protocolReference != null) {
        protocolReference
    } else {
        val sbpProxy = SBPProxy(engineClient)
        return sbpProxy.create(
            pSBP = Party("sbp"),
            authorizationProvider = sbpSystemKeycloakUser
        ).getOrHandle { throw it }.result
    }
}

fun createUserProtocol(
    sbp: ProtocolReference,
    engineClient: EngineClientApi,
    participantData: Participant,
    sbpUser: AuthorizationProvider,
): ProtocolReference {
    val participantProxy = ParticipantProxy(engineClient)
    return participantProxy.create(
        pSBP = Party("sbp"),
        pParticipant = Party("unused"),
        name = participantData.name,
        surname = participantData.surname,
        mail = participantData.mail,
        paymentDetails = participantData.details,
        sbp = sbp,
        accountHash = participantData.accountHash,
        authorizationProvider = sbpUser
    ).getOrHandle { throw it }.result
}

fun createKeycloakUser(
    keycloakClient: SbpKeycloakClient,
    username: String,
    password: String,
    participantUUID: UUID,
    adminKeycloakUser: AuthorizationProvider,
) {
    val token = adminKeycloakUser.invoke()!!.token
    val keycloakUserModel = KeycloakUserModel(
        id = null,
        username = username,
        credentials = listOf(Credentials(value = password, temporary = false)),
        attributes = Attributes(listOf("[\"unused\"]"), listOf(participantUUID.toString())),
        enabled = true
    )
    keycloakClient.createUser(token, Body.invoke(JSON.mapper.writeValueAsString(keycloakUserModel)))
}

fun addProtocolUuidToKeycloakUser(
    keycloakClient: SbpKeycloakClient,
    username: String,
    protocolId: UUID,
    adminKeycloakUser: AuthorizationProvider,
) {
    val token = adminKeycloakUser.invoke()!!.token
    val userAttributes = keycloakClient.getUserByUsername(token, username)
    val keycloakUserModel = userAttributes[0]
    keycloakUserModel.attributes?.uuid = listOf(protocolId.toString())
    keycloakClient.updateUser(
        token,
        keycloakUserModel.id!!,
        Body.invoke(JSON.mapper.writeValueAsString(keycloakUserModel))
    )
}
