package utils

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.matchers.shouldBe
import mu.KotlinLogging
import org.openapitools.client.apis.AuthenticationApi
import org.openapitools.client.infrastructure.ClientError
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.AccessToken
import java.time.Duration
import java.util.UUID

val basePath = System.getenv("BASE_PATH") ?: "http://localhost:8080"
val authenticationApi = AuthenticationApi(basePath)

val newUserName = "TestSupplierName_${UUID.randomUUID()}"

val testUserAdmin = System.getenv("SBP_TEST_USER_ADMIN") ?: "sbp"
val testUser1 = System.getenv("SBP_TEST_USER_1") ?: "user1"
val testUser2 = System.getenv("SBP_TEST_USER_2") ?: "user2"
val testUser3 = System.getenv("SBP_TEST_USER_3") ?: "user3"

val testUserPassword = System.getenv("SBP_TEST_USERS_PASSWORD") ?: "sbp"

val user = System.getenv("SBP_TEST_USER_NEW") ?: newUserName
val password = System.getenv("SBP_TEST_PASSWORD_NEW") ?: "testsupplierpass"

val logger = KotlinLogging.logger { }

fun <T> retry(retries: Int, retryPeriod: Duration, f: () -> T): T {
    for (i in 1..retries) {
        runCatching {
            return f()
        }.onFailure {
            if (i == retries) {
                logger.error(it) {}
                throw it
            }
            Thread.sleep(retryPeriod.toMillis())
            logger.warn { "Retrying $i of $retries after error: ${it.message}" }
        }
    }
    throw IllegalStateException("Unreachable")
}

fun loginAsAdmin() = login(testUserAdmin, testUserPassword)
fun loginAsUser1() = login(testUser1, testUserPassword)
fun loginAsUser2() = login(testUser2, testUserPassword)
fun loginAsUser3() = login(testUser3, testUserPassword)
fun loginAsNewlyCreatedUser() = login(user, password)

private fun login(username: String, password: String): AccessToken = retry(10, Duration.ofSeconds(2)) {
    try {
        authenticationApi.login("password", username, password)
    } catch (e: Throwable) {
        logger.error(e) { "Login failed" }
        throw e
    }
}

fun logout(refreshToken: String) = retry(10, Duration.ofSeconds(2)) {
    try {
        authenticationApi.logout(refreshToken)
    } catch (e: Throwable) {
        logger.error(e) { "Logout failed" }
        throw e
    }
}

internal fun verifyClientException(
    e: Exception,
    statusCode: Int,
    message: String,
    errorMessage: String,
) {
    e shouldBe ClientException("Client error : $message", statusCode)
    val clientException = e as ClientException
    val clientError = clientException.response as ClientError<*>
    val errorBody = clientError.body as String
    errorBody.shouldContainJsonKeyValue("$.message", errorMessage)
}

internal fun verifyCannotUpdatePriceOfBoughtOffer(e: Exception) =
    verifyClientException(e, 400, "400 Bad Request", "Offer is not in an open state.")

internal fun verifyCannotUpdatePriceOfOtherUsersOffer(e: Exception) =
    verifyClientException(e, 400, "400 Bad Request", "Offer does not belong to the supplier.")
