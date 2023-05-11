package http

import io.kotest.core.spec.style.FunSpec
import org.openapitools.client.infrastructure.ApiClient
import utils.loginAsUser1

class LoginTest : FunSpec({

    test("Login using test user") {
        ApiClient.accessToken = loginAsUser1().accessToken
    }
})
