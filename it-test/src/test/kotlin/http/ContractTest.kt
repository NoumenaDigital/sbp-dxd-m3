package http

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.openapitools.client.apis.ContractApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.infrastructure.ClientError
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.infrastructure.ServerException
import org.openapitools.client.models.Amount
import org.openapitools.client.models.ContractRequest
import org.openapitools.client.models.DebtorData
import org.openapitools.client.models.MilestoneRequest
import org.openapitools.client.models.OfferPriceDetails
import org.openapitools.client.models.ValidationErrors
import utils.Config
import utils.basePath
import utils.loginAsUser1
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

private val config = Config()

class ContractTest : FunSpec({
    val contractAPI = ContractApi(basePath)
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val validationErrorsAdapter = moshi.adapter(ValidationErrors::class.java)

    context("Contracts endpoint is allowed").config(enabled = !config.disableContracts) {
        ApiClient.accessToken = loginAsUser1().accessToken

        test("Create contract ok") {
            val response = contractAPI.createContract(contractRequest)
            response.id shouldBe contractRequest.id
            val milestone1 = response.milestones?.find { it.details.name == "Milestone 1" }!!
            val milestone2 = response.milestones?.find { it.details.name == "Milestone 2" }!!
            milestone1.details.toBeTraded shouldBe false
            milestone1.details.offerAmount shouldBe null
            milestone2.details.toBeTraded shouldBe true
            milestone2.details.offerAmount shouldBe Amount(BigDecimal(8000), "EUR")
            response.signDate shouldBe null
        }

        test("Create contract without ERP reference.") {
            try {
                contractAPI.createContract(malformedContractRequest)
            } catch (e: ClientException) {
                val errorResponse = e.response as ClientError<*>
                val validationErrors = validationErrorsAdapter.fromJson(errorResponse.body as String)
                e.statusCode shouldBe 400
                validationErrors!!.errors!![0].message shouldBe "ERP reference is required."
            }
        }

        test("Get contracts ok") {
            val invoiceUUID = contractAPI.createContract(contractRequest).uuid
            val response = contractAPI.getContracts()
            response.contracts shouldNotBe null
            response.contracts!!.size shouldBeGreaterThan 0
            response.contracts!!.find { x -> x.uuid?.equals(invoiceUUID) ?: false } shouldNotBe null
        }

        test("Get contract by ID ok") {
            val contractUUID = contractAPI.createContract(contractRequest).uuid!!
            val response = contractAPI.getContract(contractUUID)
            response.uuid shouldBe contractUUID
            response.status shouldBe "createdBySupplier"
        }
        test("Get contract by ID not found") {
            try {
                contractAPI.getContract(UUID.randomUUID())
                fail("Contract is returned but it should not be found")
            } catch (e: ClientException) {
                e.statusCode shouldBe 404
            }
        }

        test("Sign contract ok") {
            val contractUUID = contractAPI.createContract(contractRequest).uuid!!
            val response = contractAPI.signContract(contractUUID)
            response.uuid shouldBe contractUUID
            response.status shouldBe "creationFinalised"
            response.signDate shouldNotBe null
        }
        test("Sign contract amounts don't match") {
            val contractUUID = contractAPI.createContract(contractRequest).uuid!!
            try {
                contractAPI.signContract(contractUUID)
            } catch (e: ClientException) {
                e.statusCode shouldBe 400
            }
        }

        test("Get milestone by ID ok") {
            val contractUUID = contractAPI.createContract(contractRequest).uuid!!
            val milestoneUUID = contractAPI.getContract(contractUUID).milestones!![0].uuid
            val response = contractAPI.getMilestone(contractUUID, milestoneUUID)
            response.uuid shouldBe milestoneUUID
            response.milestoneDetails?.status shouldBe "created"
        }
        test("Get milestone by ID not found") {
            try {
                contractAPI.getMilestone(UUID.randomUUID(), UUID.randomUUID())
                fail("Milestone is returned but it should not be found")
            } catch (e: ClientException) {
                e.statusCode shouldBe 404
            }
        }

        test("Close milestone ok") {
            val contractUUID = contractAPI.createContract(contractRequest).uuid!!
            val milestoneUUID = contractAPI.getContract(contractUUID).milestones!![0].uuid
            contractAPI.signContract(contractUUID)
            val closeResponse = contractAPI.closeMilestone(contractUUID, milestoneUUID)
            closeResponse.uuid shouldBe milestoneUUID
            closeResponse.details.status shouldBe "reached"
            if (config.includeIpfs) {
                closeResponse.details.blockchainReference shouldNotBe null
            } else closeResponse.details.blockchainReference shouldBe null
            val contractResponse = contractAPI.getContract(contractUUID)
            val milestone = contractResponse.milestones?.find { it.uuid == milestoneUUID }!!
            milestone.details.status shouldBe "reached"
            milestone.details.originalInvoiceUUID shouldNotBe null
            contractResponse.completionPercentage shouldBe 50
        }

        test("update offer price for milestone ok") {
            val newOfferAmount = Amount(BigDecimal(6000), "EUR")
            val contractUUID = contractAPI.createContract(contractRequest).uuid!!
            val milestoneUUID = contractAPI.getContract(contractUUID).milestones!![1].uuid
            contractAPI.signContract(contractUUID)
            contractAPI.updateOfferPriceMilestone(contractUUID, milestoneUUID, OfferPriceDetails(newOfferAmount))
            val milestone = contractAPI.getContract(contractUUID).milestones?.find { it.uuid == milestoneUUID }
            milestone!!.details.offerAmount shouldBe newOfferAmount
        }

        test("update offer price for milestone milestone not marked for trading") {
            val newOfferAmount = Amount(BigDecimal(6000), "EUR")
            val contractUUID = contractAPI.createContract(contractRequest).uuid!!
            val milestoneUUID = contractAPI.getContract(contractUUID).milestones!![0].uuid
            contractAPI.signContract(contractUUID)
            try {
                contractAPI.updateOfferPriceMilestone(contractUUID, milestoneUUID, OfferPriceDetails(newOfferAmount))
            } catch (e: ClientException) {
                e.statusCode shouldBe 400
            }
        }
    }

    context("Contracts endpoint is blocked").config(enabled = config.disableContracts) {
        ApiClient.accessToken = loginAsUser1().accessToken

        test("Create contract not found") {
            try {
                contractAPI.createContract(contractRequest)
                fail("This endpoint should be blocked")
            } catch (e: ServerException) {
                e.statusCode shouldBe 501
            }
        }

        test("Get contracts not found") {
            try {
                contractAPI.getContracts()
                fail("This endpoint should be blocked")
            } catch (e: ServerException) {
                e.statusCode shouldBe 501
            }
        }

        test("Get contract by ID not found") {
            try {
                contractAPI.getContract(UUID.randomUUID())
                fail("This endpoint should be blocked")
            } catch (e: ServerException) {
                e.statusCode shouldBe 501
            }
        }

        test("Sign contract not found") {
            try {
                contractAPI.signContract(UUID.randomUUID())
                fail("This endpoint should be blocked")
            } catch (e: ServerException) {
                e.statusCode shouldBe 501
            }
        }

        test("Get milestone by ID not found") {
            try {
                contractAPI.getMilestone(UUID.randomUUID(), UUID.randomUUID())
                fail("This endpoint should be blocked")
            } catch (e: ServerException) {
                e.statusCode shouldBe 501
            }
        }

        test("Close milestone not found") {
            try {
                contractAPI.closeMilestone(UUID.randomUUID(), UUID.randomUUID())
                fail("This endpoint should be blocked")
            } catch (e: ServerException) {
                e.statusCode shouldBe 501
            }
        }
    }
})

private val contractRequest = ContractRequest(
    id = "ERP1234",
    ccy = "EUR",
    name = "Test contract",
    creationDate = OffsetDateTime.of(2022, 10, 25, 0, 0, 0, 0, ZoneOffset.UTC),
    goodsValue = Amount(
        BigDecimal.valueOf(20000),
        "EUR"
    ),
    customerDetails = DebtorData(
        name = "Debtor",
        address = "Debtor 1, Address 11111"
    ),
    expectedDeliveryDate = OffsetDateTime.of(2022, 11, 20, 0, 0, 0, 0, ZoneOffset.UTC),
    milestones = listOf(
        MilestoneRequest(
            name = "Milestone 1",
            description = "Goods prepared for transport",
            amount = Amount(BigDecimal(11000), "EUR"),
            toBeTraded = false,
            paymentPeriod = "P21D"
        ),
        MilestoneRequest(
            name = "Milestone 2",
            description = "Goods transported",
            amount = Amount(BigDecimal(9000), "EUR"),
            toBeTraded = true,
            offerAmount = Amount(BigDecimal(8000), "EUR"),
            paymentPeriod = "P1M"
        )
    )
)

private val malformedContractRequest = ContractRequest(
    id = "",
    ccy = "EUR",
    name = "Test contract",
    creationDate = OffsetDateTime.of(2022, 10, 25, 0, 0, 0, 0, ZoneOffset.UTC),
    goodsValue = Amount(BigDecimal.valueOf(20000), "EUR"),
    customerDetails = DebtorData(
        name = "Debtor",
        address = "Debtor 1, Address 11111"
    ),
    expectedDeliveryDate = OffsetDateTime.of(2022, 11, 20, 0, 0, 0, 0, ZoneOffset.UTC),
    milestones = listOf(
        MilestoneRequest(
            name = "Milestone 1",
            description = "Goods prepared for transport",
            amount = Amount(BigDecimal(11000), "EUR"),
            toBeTraded = false,
            paymentPeriod = "P21D"
        ),
        MilestoneRequest(
            name = "Milestone 2",
            description = "Goods transported",
            amount = Amount(BigDecimal(9000), "EUR"),
            toBeTraded = false,
            paymentPeriod = "P1M"
        )
    )
)
