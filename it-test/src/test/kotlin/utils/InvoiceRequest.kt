package utils

import org.openapitools.client.models.Amount
import org.openapitools.client.models.CreditorData
import org.openapitools.client.models.DebtorData
import org.openapitools.client.models.InvoiceRequest
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset

val user1CreditorData = CreditorData(
    iban = "ZZ0000000000000001",
    name = "User 1",
    address = "User 1, Address 1111"
)

val user2CreditorData = CreditorData(
    iban = "ZZ0000000000000002",
    name = "User 2",
    address = "User 2, Address 2222"
)

val user3CreditorData = CreditorData(
    iban = "ZZ0000000000000003",
    name = "User 3",
    address = "User 3, Address 3333"
)

fun invoiceRequest(
    toBeTraded: Boolean = true,
    invoiceNumber: String = "INV_${System.currentTimeMillis()}",
    creditorData: CreditorData = user1CreditorData,
    issueDateTime: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    deadline: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
): InvoiceRequest {
    val offerAmount = if (toBeTraded) Amount(
        amount = BigDecimal.valueOf(100),
        unit = "EUR"
    )
    else null
    return InvoiceRequest(
        invoiceNumber = invoiceNumber,
        amount = Amount(
            amount = BigDecimal.valueOf(112.50),
            unit = "EUR"
        ),
        creditorData = creditorData,
        debtorData = DebtorData(
            name = "Debtor",
            address = "Debtor 1, Address 11111"
        ),
        issueDateTime = issueDateTime,
        deadline = deadline,
        freeTextDescription = "free text here",
        toBeTraded = toBeTraded,
        offerAmount = offerAmount
    )
}
