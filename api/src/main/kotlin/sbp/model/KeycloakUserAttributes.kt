package sbp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Body
import org.http4k.format.Jackson.auto

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakUserModel(
    val id: String?,
    val username: String?,
    val attributes: Attributes?,
    val credentials: List<Credentials>?,
    val enabled: Boolean
)

data class Attributes(
    val party: List<String>,
    @JsonProperty("uuid")
    var uuid: List<String>?
)

data class Credentials(
    val type: String = "password",
    val value: String,
    val temporary: Boolean
)

val keycloakUserModelsLens = Body.auto<List<KeycloakUserModel>>().toLens()
