package http

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.openapitools.client.apis.InvoiceApi
import org.openapitools.client.apis.UserApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.models.CreditorData
import org.openapitools.client.models.PaymentDetails
import org.openapitools.client.models.UserCreateModel
import utils.Config
import utils.basePath
import utils.invoiceRequest
import utils.loginAsAdmin
import utils.loginAsNewlyCreatedUser
import utils.newUserName

private val config = Config()

class UserTest : FunSpec({
    val userApi = UserApi(basePath)
    val invoiceApi = InvoiceApi(basePath)
    context("create new supplier via api and test invoice creation flow") {
        ApiClient.accessToken = loginAsAdmin().accessToken
        val userCreateModelRequest = userCreateModelRequest(newUserName)

        test("create user with supplier role success") {
            val userDetailsResponse = userApi.createUser(userCreateModelRequest)

            userDetailsResponse.accountHash shouldBe userCreateModelRequest.accountHash
        }

        test("login as newly created user success") {
            ApiClient.accessToken = loginAsNewlyCreatedUser().accessToken
        }

        test("create invoice not traded success") {
            val invoiceRequest = invoiceRequest(
                false,
                "F2209840547",
                CreditorData(
                    userCreateModelRequest.details.iban!!,
                    userCreateModelRequest.details.name!!,
                    userCreateModelRequest.details.address!!
                )
            )
            val response = invoiceApi.createInvoice(invoiceRequest)
            response.invoiceNumber shouldBe invoiceRequest.invoiceNumber
            if (config.includeIpfs) {
                response.blockchainReference shouldNotBe null
                response.toBeTraded shouldBe false
                response.offerAmount shouldBe null
            } else response.blockchainReference shouldBe null
        }
    }
})

private fun userCreateModelRequest(name: String): UserCreateModel {
    return UserCreateModel(
        name = name,
        surname = "TestSupplierSurname",
        mail = "test.supplier.mail@mail.com",
        details = PaymentDetails(
            name = "TestSupplierBusinessName",
            iban = "TestSupplierIban",
            address = "TestSupplierAddress"
        ),
        accountHash = "hashtestsupplier123",
        password = "testsupplierpass"
    )
}
