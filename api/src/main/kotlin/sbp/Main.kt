package sbp

import casper.AddDelegateCargo
import casper.CasperContractClient
import casper.ContractClient
import casper.casperRoutes
import com.noumenadigital.codegen.ProtocolReference
import com.noumenadigital.platform.engine.client.AuthConfig
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi
import com.noumenadigital.platform.engine.client.TokenAuthorizationProvider
import com.noumenadigital.platform.engine.client.UserConfig
import config.CasperCredential
import config.ISbpdxdConfiguration
import config.SbpdxdConfiguration
import io.prometheus.client.hotspot.DefaultExports
import ipfs.DefaultIpfsStore
import ipfs.IpfsStore
import keycloak.SbpKeycloakClient
import keycloak.SbpKeycloakClientImpl
import keycloak.SbpPartyProvider
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.KtorCIO
import org.http4k.server.asServer
import sbp.controllers.contractRoutes
import sbp.controllers.invoiceRoutes
import sbp.controllers.loggedInUserRoutes
import sbp.controllers.offerRoutes
import sbp.controllers.userRoutes
import sbp.filter.RoleFilter
import sbp.filter.blockedEndpointsFilter
import sbp.filter.sbpdxdErrorFilter
import sbp.model.Participant
import sbp.model.User1
import sbp.model.User2
import sbp.model.User3
import seed.config.Configuration
import seed.filter.accessLogFilter
import seed.filter.corsFilter
import seed.filter.loginRequired
import seed.graphql.GraphQLClient
import seed.keycloak.KeycloakClient
import seed.keycloak.KeycloakClientImpl
import seed.keycloak.KeycloakForwardAuthorization
import seed.metrics.measure
import seed.security.AuthHandler
import seed.security.FormKeycloakAuthHandler
import seed.security.ForwardAuthorization
import seed.server.admin
import seed.server.loginRoutes
import sign.CasperSigner
import sign.OnChainClient
import sign.SignedIpfsOnChainClient
import sign.Signer
import util.addProtocolUuidToKeycloakUser
import util.createUserProtocol
import util.initSbp
import java.net.URL
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

fun main(): Unit = runBlocking {
    logger.info { "Starting API" }

    DefaultExports.initialize()

    val sbpSystemUser = System.getenv("SBP_SYSTEM_USER") ?: "sbp"
    val sbpSystemPassword = System.getenv("SBP_SYSTEM_PASSWORD") ?: "sbp"
    val masterRealm = System.getenv("MASTER_REALM") ?: ""
    val masterClientId = System.getenv("MASTER_CLIENT_ID") ?: ""
    val keycloakAdminUser = System.getenv("KEYCLOAK_ADMIN_USER") ?: ""
    val keycloakAdminPassword = System.getenv("KEYCLOAK_ADMIN_PASSWORD") ?: ""

    val httpPort = (System.getenv("HTTP_PORT") ?: "8080").toInt()
    val adminPort = (System.getenv("HTTP_ADMIN_PORT") ?: "8000").toInt()
    val config: ISbpdxdConfiguration = SbpdxdConfiguration(
        baseConfig = Configuration(
            keycloakRealm = System.getenv("KEYCLOAK_REALM") ?: "sbp",
            keycloakClientId = System.getenv("KEYCLOAK_CLIENT_ID") ?: "sbp",
        )
    )

    val adminServer = admin(config).asServer(KtorCIO(adminPort))
    adminServer.start()
    val engineClient = EngineClientApi(config.engineURL)
    val graphQLClient = GraphQLClient(URL(config.readModelURL))
    val casperClient: ContractClient = CasperContractClient(config)
    val baseKeycloakClient: KeycloakClient = KeycloakClientImpl(config = config, partyProvider = SbpPartyProvider())
    val keycloakClient: SbpKeycloakClient =
        SbpKeycloakClientImpl(config = config, baseClient = baseKeycloakClient)
    val forwardAuthorization: ForwardAuthorization = KeycloakForwardAuthorization(baseKeycloakClient)

    val sbpUser: AuthorizationProvider =
        TokenAuthorizationProvider(
            UserConfig(sbpSystemUser, sbpSystemPassword),
            AuthConfig(config.keycloakRealm, config.keycloakURL.toExternalForm(), config.keycloakClientId)
        )

    val adminKeycloakUser: AuthorizationProvider =
        TokenAuthorizationProvider(
            UserConfig(keycloakAdminUser, keycloakAdminPassword),
            AuthConfig(masterRealm, config.keycloakURL.toExternalForm(), masterClientId)
        )

    val sbp = initSbp(engineClient, graphQLClient, sbpUser)
    logger.info { "Init sbp uuid: ${sbp.id}" }

    val initializeUser = existingKeycloakUserInitializer(
        sbp = sbp,
        keycloakClient = keycloakClient,
        engineClient = engineClient,
        casperClient = casperClient,
        sbpAccountHash = config.accountHashSbp,
        sbpUser = sbpUser,
        adminKeycloakUser = adminKeycloakUser,
    )

    val user1CasperCredential = config.run { CasperCredential(privateKeyUser1, publicKeyUser1, accountHashUser1) }
    val user2CasperCredential = config.run { CasperCredential(privateKeyUser2, publicKeyUser2, accountHashUser2) }
    val user3CasperCredential = config.run { CasperCredential(privateKeyUser3, publicKeyUser3, accountHashUser3) }

    initializeUser(User1, "user1" to user1CasperCredential)
    initializeUser(User2, "user2" to user2CasperCredential)
    initializeUser(User3, "user3" to user3CasperCredential)

    val casperPrivateKey =
        """
        -----BEGIN PRIVATE KEY-----
        ${config.privateKeySbp}
        -----END PRIVATE KEY-----
        """.trimIndent()
    val signer: Signer = CasperSigner(casperPrivateKey)
    val ipfsClient: IpfsStore = DefaultIpfsStore(config)
    val onChainClient: OnChainClient =
        SignedIpfsOnChainClient(ipfsClient = ipfsClient, signer = signer, config = config)

    val sbpRoleOnly = RoleFilter(forwardAuthorization, listOf("sbp"))
    val authHandler: AuthHandler = FormKeycloakAuthHandler(config)

    val userRoutes =
        userRoutes(engineClient, sbp, keycloakClient, sbpUser, adminKeycloakUser)
    val loggedInUserRoutes = loggedInUserRoutes(engineClient, forwardAuthorization)
    val invoiceRoutes =
        invoiceRoutes(config, engineClient, sbp, onChainClient, forwardAuthorization, sbpUser)
    val contractRoutes =
        contractRoutes(config, engineClient, onChainClient, forwardAuthorization, sbpUser)
    val offerRoutes = offerRoutes(engineClient, sbp, forwardAuthorization, sbpUser)
    val casperRoutes = casperRoutes(casperClient)

    val individuallyDecoratedRoutes =
        routes(
            loginRoutes(config, authHandler),

            loginRequired(config)
                .then(sbpRoleOnly)
                .then(userRoutes),

            loginRequired(config)
                .then(loggedInUserRoutes),

            invoiceRoutes,

            blockedEndpointsFilter(config)
                .then(contractRoutes),

            loginRequired(config)
                .then(offerRoutes),

            casperRoutes
        )

    val globallyDecoratedRoutes =
        measure()
            .then(accessLogFilter(config))
            .then(corsFilter(config))
            .then(sbpdxdErrorFilter(true))
            .then(individuallyDecoratedRoutes)

    val appServer = globallyDecoratedRoutes.asServer(KtorCIO(httpPort))

    logger.info { "Request logging verbosity is ${config.accessLogVerbosity}" }
    exitProcess(
        try {
            appServer.start().block()
            0
        } catch (e: Throwable) {
            logger.error(e) {}
            1
        } finally {
            adminServer.stop()
        }
    )
}

fun existingKeycloakUserInitializer(
    sbp: ProtocolReference,
    keycloakClient: SbpKeycloakClient,
    engineClient: EngineClientApi,
    casperClient: ContractClient,
    sbpAccountHash: String,
    sbpUser: AuthorizationProvider,
    adminKeycloakUser: AuthorizationProvider,
): (participantData: Participant, credentials: Pair<String, CasperCredential>) -> Unit =
    { data, credentials ->

        val keycloakUsername = credentials.first
        val casperCredential = credentials.second

        data.accountHash = casperCredential.accountHash

        val userProtocol = createUserProtocol(
            sbp = sbp,
            engineClient = engineClient,
            participantData = data,
            sbpUser = sbpUser
        )

        val cargo = AddDelegateCargo(
            privateKeyIdentity = casperCredential.privateKey,
            publicKeyIdentity = casperCredential.publicKey,
            didOwnerAccountHash = casperCredential.accountHash,
            delegateAccountHash = sbpAccountHash
        )

        casperClient.addDelegate(cargo)

        addProtocolUuidToKeycloakUser(
            keycloakClient = keycloakClient,
            username = keycloakUsername,
            protocolId = userProtocol.id,
            adminKeycloakUser = adminKeycloakUser
        )

        logger.info { "User created with the [username: $keycloakUsername, uuid: ${userProtocol.id}]" }
    }
