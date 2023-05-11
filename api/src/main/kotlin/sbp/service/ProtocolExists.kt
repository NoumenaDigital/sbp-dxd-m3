package sbp.service

import com.noumenadigital.codegen.ProtocolReference
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import config.JSON
import seed.graphql.GraphQLClient
import seed.graphql.GraphQlResponse
import seed.graphql.getProtocolIdQuery
import java.util.UUID

class ProtocolExists(
    private val graphQLClient: GraphQLClient,
    private val prototypeId: String,
    private val sbpSystemKeycloakUser: AuthorizationProvider,
) {
    fun getProtocolReference(): ProtocolReference? {
        val queryResponse = graphQLClient.handle(getProtocolIdQuery(prototypeId), sbpSystemKeycloakUser)
        val graphQlResponse = JSON.mapper.readValue(queryResponse, GraphQlResponse::class.java)
        val protocolId = graphQlResponse.data.protocolStates.nodes.firstOrNull()?.protocolId
        if (protocolId.isNullOrBlank()) return null
        return ProtocolReference(UUID.fromString(protocolId), prototypeId)
    }
}
