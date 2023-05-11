package keycloak

import io.prometheus.client.Summary
import mu.KotlinLogging
import org.http4k.client.ApacheClient
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import sbp.model.KeycloakUserModel
import sbp.model.keycloakUserModelsLens
import seed.config.IConfiguration
import seed.filter.ErrorCode
import seed.keycloak.KeycloakClient
import seed.keycloak.KeycloakServerException
import seed.keycloak.KeycloakUnauthorizedException
import seed.metrics.record
import java.net.URL

internal val logger = KotlinLogging.logger {}

private fun Request.optionalHeader(name: String, value: String?): Request {
    if (value == null) {
        return this
    }
    return this.header(name, value)
}

interface SbpKeycloakClient : KeycloakClient {
    fun getUserByUsername(bearerToken: String, username: String): List<KeycloakUserModel>
    fun createUser(bearerToken: String, body: Body)
    fun updateUser(bearerToken: String, userId: String, body: Body)
}

class SbpKeycloakClientImpl(
    config: IConfiguration,
    val client: HttpHandler = ApacheClient(),
    private val baseClient: KeycloakClient,
) : SbpKeycloakClient, KeycloakClient by baseClient {
    private val base = config.keycloakURL
    private val host = config.keycloakHost
    private val realm = config.keycloakRealm

    private val endpointUser = URL(base, "/admin/realms/$realm/users").toExternalForm()

    override fun getUserByUsername(bearerToken: String, username: String): List<KeycloakUserModel> {
        return keycloakTimer.labels("getByName").record {
            val req = Request(Method.GET, "$endpointUser?username=$username")
                .header("Authorization", "Bearer $bearerToken")
                .optionalHeader("Host", host)
            val res = client(req)

            when (res.status) {
                Status.OK -> keycloakUserModelsLens.extract(res)
                Status.UNAUTHORIZED -> throw KeycloakUnauthorizedException(ErrorCode.InvalidLogin)
                else -> {
                    logger.error("Unexpected Keycloak response: ${res.status} - ${res.bodyString()}")
                    throw KeycloakServerException(ErrorCode.InternalServerError)
                }
            }
        }
    }

    override fun createUser(bearerToken: String, body: Body) {
        keycloakTimer.labels("create").record {
            val req = Request(Method.POST, endpointUser)
                .header("Content-Type", ContentType.APPLICATION_JSON.toHeaderValue())
                .header("Authorization", "Bearer $bearerToken")
                .optionalHeader("Host", host)
                .body(body)
            val res = client(req)

            if (res.status == Status.CREATED) {
                logger.info("Successfully create user in Keycloak")
            } else {
                logger.error("Unexpected Keycloak response while creating the user ${res.status} - ${res.bodyString()}")
                throw KeycloakServerException(ErrorCode.InternalServerError)
            }
        }
    }

    override fun updateUser(bearerToken: String, userId: String, body: Body) {
        keycloakTimer.labels("update").record {
            val req = Request(Method.PUT, "$endpointUser/$userId")
                .header("Content-Type", ContentType.APPLICATION_JSON.toHeaderValue())
                .header("Authorization", "Bearer $bearerToken")
                .optionalHeader("Host", host)
                .body(body)
            val res = client(req)

            if (res.status == Status.NO_CONTENT) {
                logger.info("Successfully updated user $userId in Keycloak")
            } else {
                logger.error("Unexpected Keycloak response while updating the user with id $userId: ${res.status} - ${res.bodyString()}")
                throw KeycloakServerException(ErrorCode.InternalServerError)
            }
        }
    }

    companion object {
        private val keycloakTimer = Summary.build()
            .name("sbpdxd_keycloak_security_functions_seconds")
            .help("record time taken for invoking keycloak security functions")
            .labelNames("function")
            .quantile(0.5, 0.01).quantile(0.9, 0.01).quantile(0.99, 0.005)
            .register()
    }
}
