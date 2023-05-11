package keycloak

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.noumenadigital.platform.engine.values.ClientPartyValue
import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.lens.LensFailure
import seed.config.SnakeCaseJsonConfiguration.auto
import seed.keycloak.PartyProvider
import java.util.UUID

fun uuid(party: ClientPartyValue): UUID {
    val uuids = party.access.getValue("uuid")
    val uuid = uuids.first()
    return UUID.fromString(uuid)
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
data class SbpUserInfo(
    val preferredUsername: String,
    val uuid: UUID?,
    val party: List<String>,
)

internal val keycloakUserInfoLens = Body.auto<SbpUserInfo>().toLens()

internal class InvalidUserConfiguration(e: LensFailure) :
    RuntimeException("The party used for this request is incorrectly configured", e)

class SbpPartyProvider : PartyProvider {
    override fun party(response: Response): ClientPartyValue {
        val userInfo: SbpUserInfo
        try {
            userInfo = keycloakUserInfoLens.extract(response)
        } catch (e: LensFailure) {
            throw InvalidUserConfiguration(e)
        }

        return ClientPartyValue(
            entity = mapOf(
                "preferred_username" to setOf(userInfo.preferredUsername)
            ),
            access = mapOf(
                "uuid" to setOf(userInfo.uuid.toString()),
                "party" to userInfo.party.toSet()
            )
        )
    }
}
