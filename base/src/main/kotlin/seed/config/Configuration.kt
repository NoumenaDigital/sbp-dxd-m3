package seed.config

import seed.filter.AccessLogVerbosity
import java.net.URL

interface IConfiguration {
    val keycloakRealm: String
    val keycloakClientId: String
    val allowedOrigins: List<String>
    val keycloakURL: URL
    val engineURL: String
    val readModelURL: String
    val accessLogVerbosity: AccessLogVerbosity
    val apiServerUrl: String

    val keycloakHost: String?
}

data class Configuration(
    override val keycloakRealm: String,
    override val keycloakClientId: String,
    override val allowedOrigins: List<String> = (System.getenv("CORS_ALLOWED_ORIGINS") ?: "").split(","),
    override val keycloakURL: URL = URL(System.getenv("KEYCLOAK_URL") ?: "http://localhost:11000"),
    override val engineURL: String = System.getenv("ENGINE_URL") ?: "http://localhost:12000",
    override val readModelURL: String = System.getenv("READ_MODEL_URL") ?: "http://localhost:15000",
    override val accessLogVerbosity: AccessLogVerbosity =
        AccessLogVerbosity.valueOf(System.getenv("ACCESS_LOG_VERBOSITY") ?: "NONE"),
    override val apiServerUrl: String = System.getenv("API_SERVER_URL") ?: "http://localhost:8080",

    // special case for development from outside docker
    override val keycloakHost: String? = if (keycloakURL.host == "localhost") "keycloak:${keycloakURL.port}" else null,
) : IConfiguration
