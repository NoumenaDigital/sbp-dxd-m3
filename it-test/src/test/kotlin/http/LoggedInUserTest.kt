package http

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.openapitools.client.apis.UserApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.PaymentDetails
import org.openapitools.client.models.UserDetails
import utils.basePath
import utils.loginAsUser1
import utils.loginAsUser3

class LoggedInUserTest : FunSpec({
    val userApi = UserApi(basePath)

    context("Get logged in user data") {
        test("Get logged in user1 data") {
            ApiClient.accessToken = loginAsUser1().accessToken
            val actual = userApi.getLoggedInUser()

            val expected = UserDetails(
                name = "User",
                surname = "1",
                mail = "user1@mail.com",
                details = PaymentDetails(
                    iban = "ZZ0000000000000001",
                    name = "User 1",
                    address = "User 1, Address 1111"
                ),
                accountHash = "d6c48c5c2378e30905bb7916cde5fe5fcae082a0e3eb5093d7435cc7c800bee7"
            )

            actual.name shouldBe expected.name
            actual.surname shouldBe expected.surname
            actual.mail shouldBe expected.mail
            actual.details shouldBe expected.details
            actual.accountHash shouldBe expected.accountHash
        }

        test("Get logged in user3 data") {
            ApiClient.accessToken = loginAsUser3().accessToken

            val actual = userApi.getLoggedInUser()

            val expected = UserDetails(
                name = "User",
                surname = "3",
                mail = "user3@mail.com",
                details = PaymentDetails(
                    iban = "ZZ0000000000000003",
                    name = "User 3",
                    address = "User 3, Address 3333"
                ),
                accountHash = "4af7048197bc3f13f8be2d705335f9a291a0905cb0d2505d87962e8ed5aa5dab"
            )

            actual.name shouldBe expected.name
            actual.surname shouldBe expected.surname
            actual.mail shouldBe expected.mail
            actual.details shouldBe expected.details
            actual.accountHash shouldBe expected.accountHash
        }
    }
})
