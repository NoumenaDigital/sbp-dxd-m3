package http

import io.kotest.assertions.fail
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.openapitools.client.apis.InvoiceApi
import org.openapitools.client.apis.OfferApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.infrastructure.ClientError
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.AccessToken
import org.openapitools.client.models.Amount
import org.openapitools.client.models.CreditorData
import org.openapitools.client.models.OfferPriceDetails
import org.openapitools.client.models.OfferResponse
import utils.basePath
import utils.invoiceRequest
import utils.loginAsUser1
import utils.loginAsUser2
import utils.loginAsUser3
import utils.user1CreditorData
import utils.user2CreditorData
import utils.user3CreditorData
import utils.verifyCannotUpdatePriceOfBoughtOffer
import utils.verifyCannotUpdatePriceOfOtherUsersOffer
import java.math.BigDecimal
import java.util.UUID

class OffersTest : FunSpec({
    val invoiceAPI = InvoiceApi(basePath)
    val offerAPI = OfferApi(basePath)

    val nonExistentId = UUID.fromString("e9c07771-423c-449c-b001-420e9b6428fe")

    fun createOffer(accessToken: AccessToken, creditorData: CreditorData): OfferResponse {
        ApiClient.accessToken = accessToken.accessToken
        val invoiceRequest = invoiceRequest(creditorData = creditorData)
        val invoiceResponse = invoiceAPI.createInvoice(invoiceRequest)
        invoiceResponse.invoiceNumber shouldBe invoiceRequest.invoiceNumber

        val offerResponses = offerAPI.getMyIssuedOffers().offers

        return offerResponses.first { it.invoiceNumber == invoiceRequest.invoiceNumber }
    }

    context("everyone can see marketplace (openOffers)") {
        fun verifyUserCanSeeMarketPlaceOffers(
            offerCreator: AccessToken,
            creditorData: CreditorData,
            offerReader: AccessToken,
        ) {
            // given
            val offer = createOffer(offerCreator, creditorData)

            // when
            ApiClient.accessToken = offerReader.accessToken
            val offersResponse = offerAPI.getMarketplaceOffers()

            // then
            val offerIds = offersResponse.offers.map { it.uuid }
            offerIds shouldContain offer.uuid
        }

        test("User3 can see User1 created offers") {
            verifyUserCanSeeMarketPlaceOffers(loginAsUser1(), user1CreditorData, loginAsUser3())
        }
        test("User1 can see User2 created offers") {
            verifyUserCanSeeMarketPlaceOffers(loginAsUser2(), user2CreditorData, loginAsUser1())
        }
        test("User2 can see User3 created offers") {
            verifyUserCanSeeMarketPlaceOffers(loginAsUser3(), user3CreditorData, loginAsUser2())
        }
    }

    context("user lists their own bought portfolio (boughtOffers)") {
        test("User1 creates offer, User2 buys it. Check User2's portfolio contains it and it's not in marketplace anymore") {
            // given
            val offer = createOffer(loginAsUser1(), user1CreditorData)

            // when
            ApiClient.accessToken = loginAsUser2().accessToken
            offerAPI.buyOffer(offer.uuid)

            // then
            // offer should be in portfolio (boughtOffers for that user)
            val boughtOffers = offerAPI.getMyBoughtOffers().offers
            val boughtOfferIds = boughtOffers.map { it.uuid }
            boughtOfferIds shouldContain offer.uuid

            // and
            // offer should not be in marketplace (available to buy offers)
            val offersToBuy = offerAPI.getMarketplaceOffers().offers
            val offersToBuyIds = offersToBuy.map { it.uuid }
            offersToBuyIds shouldNotContain offer.uuid
        }
    }

    context("any user can see any specific offer - bought or otherwise") {
        fun verifyUserCanSeeOffer(
            offerCreator: AccessToken,
            creditorData: CreditorData,
            offerReader: AccessToken,
        ) {
            // given
            val offer = createOffer(offerCreator, creditorData)

            // when
            ApiClient.accessToken = offerReader.accessToken
            val offerResponse = offerAPI.getOfferById(offer.uuid)

            // then
            offerResponse.uuid shouldBe offer.uuid
        }

        test("User3 can see offer created by User1") {
            verifyUserCanSeeOffer(loginAsUser1(), user1CreditorData, loginAsUser3())
        }
        test("User1 can see offer created by User2") {
            verifyUserCanSeeOffer(loginAsUser2(), user2CreditorData, loginAsUser1())
        }
        test("User2 can see offer created by User3") {
            verifyUserCanSeeOffer(loginAsUser3(), user3CreditorData, loginAsUser2())
        }
        test("getOfferById returns 404 Not Found for a non-existent id") {
            ApiClient.accessToken = loginAsUser3().accessToken
            try {
                offerAPI.getOfferById(nonExistentId)
                fail("should fail for non-existent offerId")
            } catch (e: ClientException) {
                e.statusCode shouldBe 404
            }
        }
    }

    context("user lists their own issuedOffers") {
        fun verifyUserListsTheirOwnOffers(accessToken: AccessToken, creditorData: CreditorData) {
            // given
            val offer = createOffer(accessToken, creditorData)
            val offerId = offer.uuid

            // when
            val myIssuedOffers = offerAPI.getMyIssuedOffers().offers
            val myIssuedOfferIds = myIssuedOffers.map { it.uuid }

            // then
            myIssuedOfferIds shouldContain offerId
        }

        test("User3") {
            verifyUserListsTheirOwnOffers(loginAsUser3(), user3CreditorData)
        }
        test("User1") {
            verifyUserListsTheirOwnOffers(loginAsUser1(), user1CreditorData)
        }
        test("User2") {
            verifyUserListsTheirOwnOffers(loginAsUser2(), user2CreditorData)
        }
    }

    context("only the user that created the offer may update the price (and only if the offer is still open)") {
        test("can successfully update the offer price") {
            // given
            val offer = createOffer(loginAsUser2(), user2CreditorData)
            val offerId = offer.uuid
            val priceUpdate = OfferPriceDetails(Amount(BigDecimal(150), "EUR"))

            // when
            offerAPI.updateOfferPrice(offerId, priceUpdate)
            val updatedOffer = offerAPI.getOfferById(offerId)

            // then
            updatedOffer.price shouldBe priceUpdate.offerPrice
        }

        test("cannot update offer price of another user's offer") {
            // given
            val offer = createOffer(loginAsUser3(), user3CreditorData)
            val offerId = offer.uuid
            val priceUpdate = OfferPriceDetails(Amount(BigDecimal(150), "EUR"))

            // when
            ApiClient.accessToken = loginAsUser1().accessToken
            try {
                offerAPI.updateOfferPrice(offerId, priceUpdate)
                fail("should not be able to update offer price belonging to a different user")
            } catch (e: Exception) {
                // then
                verifyCannotUpdatePriceOfOtherUsersOffer(e)
            }
        }

        test("cannot update offer price when offer not found") {
            // given
            ApiClient.accessToken = loginAsUser3().accessToken
            val priceUpdate = OfferPriceDetails(Amount(BigDecimal(150), "EUR"))

            try {
                // when
                offerAPI.updateOfferPrice(nonExistentId, priceUpdate)
                fail("should not be able to update offer price when offer not found")
            } catch (e: Exception) {
                // then
                e shouldBe ClientException("Client error : 404 Not Found", 404)
                val clientException = e as ClientException
                val clientError = clientException.response as ClientError<*>
                val errorBody = clientError.body as String
                errorBody.shouldContainJsonKeyValue("$.code", "ItemNotFound")
            }
        }

        test("cannot update offer price when offer has already been bought") {
            // given
            val offer = createOffer(loginAsUser1(), user1CreditorData)
            val offerId = offer.uuid
            val priceUpdate = OfferPriceDetails(Amount(BigDecimal(150), "EUR"))

            // and
            ApiClient.accessToken = loginAsUser2().accessToken
            offerAPI.buyOffer(offer.uuid)
            ApiClient.accessToken = loginAsUser1().accessToken

            try {
                // when
                offerAPI.updateOfferPrice(offerId, priceUpdate)
                fail("should not be able to update offer price when offer has already been bought")
            } catch (e: Exception) {
                // then
                verifyCannotUpdatePriceOfBoughtOffer(e)
            }
        }
    }
})
