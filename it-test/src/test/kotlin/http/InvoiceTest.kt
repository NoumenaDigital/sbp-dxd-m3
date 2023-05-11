package http

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.openapitools.client.apis.InvoiceApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.infrastructure.ClientError
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.Amount
import org.openapitools.client.models.CreditorData
import org.openapitools.client.models.DebtorData
import org.openapitools.client.models.ErrorResponse
import org.openapitools.client.models.InvoiceRequest
import org.openapitools.client.models.InvoiceStatus
import org.openapitools.client.models.InvoiceVerificationRequest
import utils.Config
import utils.basePath
import utils.correctUblBase64Encoded
import utils.errorResponseAdapter
import utils.incorrectUblBase64Encoded
import utils.invoiceRequest
import utils.loginAsUser1
import utils.missingFieldsUblBase64Encoded
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Base64
import java.util.UUID

private val config = Config()

class InvoiceTest : FunSpec({
    val invoiceAPI = InvoiceApi(basePath)

    context("Create invoice not traded") {
        val invoiceRequest = invoiceRequest(false, deadline = OffsetDateTime.now().plusDays(2))
        ApiClient.accessToken = loginAsUser1().accessToken
        test("create invoice not traded success") {
            val response = invoiceAPI.createInvoice(invoiceRequest)
            response.invoiceNumber shouldBe invoiceRequest.invoiceNumber
            if (config.includeIpfs) {
                response.blockchainReference shouldNotBe null
                response.toBeTraded shouldBe false
                response.offerAmount shouldBe null
                response.invoiceStatus shouldBe InvoiceStatus.finalised
            } else response.blockchainReference shouldBe null
        }

        test("try creating invoice without invoice number") {
            try {
                invoiceAPI.createInvoice(malformedInvoiceRequest)
                fail("Invoice is not created")
            } catch (e: ClientException) {
                val exceptionResponse = e.response as ClientError<*>
                val errorResponse = errorResponseAdapter.fromJson(exceptionResponse.body as String)
                e.statusCode shouldBe 400
                errorResponse shouldBe ErrorResponse("Invoice number is required.")
            }
        }

        test("try creating invoice with wrong supplier data") {
            try {
                invoiceAPI.createInvoice(invoiceWithWrongSupplierData)
                fail("Invoice is not created")
            } catch (e: ClientException) {
                val errorResponse = e.response as ClientError<*>
                val error = errorResponseAdapter.fromJson(errorResponse.body as String)
                e.statusCode shouldBe 400
                error!!.message shouldBe "creditor details in the invoice do not match supplier ones"
            }
        }
    }

    context("Create invoice to be traded") {
        val invoiceRequest = invoiceRequest(true, deadline = OffsetDateTime.now().plusDays(2))
        ApiClient.accessToken = loginAsUser1().accessToken
        test("create invoice") {
            val response = invoiceAPI.createInvoice(invoiceRequest)
            response.invoiceNumber shouldBe invoiceRequest.invoiceNumber
            if (config.includeIpfs) {
                response.blockchainReference shouldNotBe null
                response.toBeTraded shouldBe true
                response.offerAmount shouldBe invoiceRequest.offerAmount
                response.invoiceStatus shouldBe InvoiceStatus.finalised
            } else response.blockchainReference shouldBe null
        }
    }

    context("Get invoices for Supplier") {
        ApiClient.accessToken = loginAsUser1().accessToken
        val invoiceRequest = invoiceRequest(false)
        test("get invoices") {
            val invoiceUUID = invoiceAPI.createInvoice(invoiceRequest).uuid
            val response = invoiceAPI.getInvoices()
            response.invoices shouldNotBe null
            response.invoices!!.size shouldBeGreaterThan 0
            response.invoices!!.find { x -> x.uuid?.equals(invoiceUUID) ?: false } shouldNotBe null
        }
    }

    context("Get invoice by ID") {
        ApiClient.accessToken = loginAsUser1().accessToken
        val invoiceRequest = invoiceRequest(false)
        test("success") {
            val invoiceUUID = invoiceAPI.createInvoice(invoiceRequest).uuid!!
            val response = invoiceAPI.getInvoice(invoiceUUID)
            response.uuid shouldBe invoiceUUID
        }
        test("invoice not found") {
            try {
                invoiceAPI.getInvoice(UUID.randomUUID())
                fail("Invoice is returned but it should not be found")
            } catch (e: ClientException) {
                e.statusCode shouldBe 404
            }
        }
    }

    context("Verify invoice") {
        ApiClient.accessToken = loginAsUser1().accessToken
        test("correct") {
            val invoiceRequest = invoiceRequest(
                toBeTraded = false,
                issueDateTime = OffsetDateTime.of(2022, 10, 25, 0, 0, 0, 0, ZoneOffset.UTC),
                deadline = OffsetDateTime.of(2022, 10, 28, 0, 0, 0, 0, ZoneOffset.UTC)
            )
            val invoiceResponse = invoiceAPI.createInvoice(invoiceRequest)
            val invoiceXmlStr = correctUblBase64Encoded(
                invoiceRequest.invoiceNumber
            )
            val invoiceBase64EncodedBytes = Base64.getEncoder().encode(invoiceXmlStr.toByteArray())
            val invoiceBase64EncodedStr = String(invoiceBase64EncodedBytes)

            val response =
                invoiceAPI.verifyInvoiceInternally(InvoiceVerificationRequest("xml", invoiceBase64EncodedStr))
            response.verified shouldBe true
        }
        test("incorrect data") {
            val invoiceXmlStr = incorrectUblBase64Encoded()
            val invoiceBase64EncodedBytes = Base64.getEncoder().encode(invoiceXmlStr.toByteArray())
            val invoiceBase64EncodedStr = String(invoiceBase64EncodedBytes)

            val response =
                invoiceAPI.verifyInvoiceInternally(InvoiceVerificationRequest("xml", invoiceBase64EncodedStr))
            response.verified shouldBe false
        }
        test("xml cannot be parsed") {
            try {
                val invoiceXmlStr = missingFieldsUblBase64Encoded()
                val invoiceBase64EncodedBytes = Base64.getEncoder().encode(invoiceXmlStr.toByteArray())
                val invoiceBase64EncodedStr = String(invoiceBase64EncodedBytes)

                invoiceAPI.verifyInvoiceInternally(InvoiceVerificationRequest("xml", invoiceBase64EncodedStr))
                fail("It should be failed earlier")
            } catch (e: ClientException) {
                e.statusCode shouldBe 400
            }
        }
        test("type is not xml") {
            try {
                val invoiceXmlStr = missingFieldsUblBase64Encoded()
                val invoiceBase64EncodedBytes = Base64.getEncoder().encode(invoiceXmlStr.toByteArray())
                val invoiceBase64EncodedStr = String(invoiceBase64EncodedBytes)

                invoiceAPI.verifyInvoiceInternally(InvoiceVerificationRequest("json", invoiceBase64EncodedStr))
                fail("It should be failed earlier")
            } catch (e: ClientException) {
                e.statusCode shouldBe 400
            }
        }
    }
})

private val malformedInvoiceRequest = InvoiceRequest(
    invoiceNumber = "",
    amount = Amount(
        amount = BigDecimal.valueOf(112.50),
        unit = "EUR"
    ),
    creditorData = CreditorData(
        iban = "ZZ0000000000000001",
        name = "User 1",
        address = "User 1, Address 1111"
    ),
    debtorData = DebtorData(
        name = "Debtor",
        address = "Debtor 1, Address 11111"
    ),
    issueDateTime = OffsetDateTime.of(2022, 10, 25, 0, 0, 0, 0, ZoneOffset.UTC),
    deadline = OffsetDateTime.of(2022, 10, 28, 0, 0, 0, 0, ZoneOffset.UTC),
    freeTextDescription = "free text here",
    toBeTraded = false
)

private val invoiceWithWrongSupplierData = InvoiceRequest(
    invoiceNumber = "F2209840543",
    amount = Amount(
        amount = BigDecimal.valueOf(112.50),
        unit = "EUR"
    ),
    creditorData = CreditorData(
        iban = "123",
        name = "User 3",
        address = "Wrong Address"
    ),
    debtorData = DebtorData(
        name = "Debtor",
        address = "Debtor 1, Address 11111"
    ),
    issueDateTime = OffsetDateTime.of(2022, 10, 25, 0, 0, 0, 0, ZoneOffset.UTC),
    deadline = OffsetDateTime.of(2022, 10, 28, 0, 0, 0, 0, ZoneOffset.UTC),
    freeTextDescription = "free text here",
    toBeTraded = false
)
