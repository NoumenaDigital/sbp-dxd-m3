package sbp.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.noumenadigital.npl.api.generated.support.AmountFacade
import com.noumenadigital.npl.api.generated.support.CCYEnum
import java.math.BigDecimal
import java.time.OffsetDateTime

const val cac = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
const val cbc = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"

@JacksonXmlRootElement
data class InvoiceXml(
    @JacksonXmlProperty(namespace = cbc, localName = "ID")
    val id: String,
    @JacksonXmlProperty(namespace = cbc, localName = "IssueDate")
    val issueDate: OffsetDateTime,
    @JacksonXmlProperty(namespace = cbc, localName = "Deadline")
    val deadline: OffsetDateTime,
    @JacksonXmlProperty(namespace = cac, localName = "Amount")
    val amount: Amount,
    @JacksonXmlProperty(namespace = cac, localName = "AccountingSupplierParty")
    val accountingSupplierParty: AccountingSupplierParty,
    @JacksonXmlProperty(namespace = cac, localName = "AccountingCustomerParty")
    val accountingCustomerParty: AccountingCustomerParty,
    @JacksonXmlProperty(namespace = cbc, localName = "FreeTextDescription")
    val freeTextDescription: String
)

data class Amount(
    @JacksonXmlProperty(namespace = cbc, localName = "Amount")
    val amount: BigDecimal,
    @JacksonXmlProperty(namespace = cbc, localName = "Unit")
    val unit: String
) {
    constructor(facade: AmountFacade) : this(
        facade.amount,
        facade.ccy.toString()
    )

    fun toFacade() = AmountFacade(amount, CCYEnum.valueOf(unit))
}

data class AccountingSupplierParty(
    @JacksonXmlProperty(namespace = cac, localName = "Party")
    val party: Party
)

data class AccountingCustomerParty(
    @JacksonXmlProperty(namespace = cac, localName = "Party")
    val party: Party
)

data class PaymentMeans(
    @JacksonXmlProperty(namespace = cac, localName = "PayeeFinancialAccount")
    val payeeFinancialAccount: PayeeFinancialAccount
)

data class Party(
    @JacksonXmlProperty(namespace = cbc, localName = "EndpointID")
    val endpointId: String?,
    @JacksonXmlProperty(namespace = cbc, localName = "IBAN")
    val iban: String?,
    @JacksonXmlProperty(namespace = cac, localName = "PartyName")
    val partyName: PartyName
)

data class PartyName(
    @JacksonXmlProperty(namespace = cbc, localName = "Name")
    val name: String?,
    @JacksonXmlProperty(namespace = cbc, localName = "Address")
    val address: String?,
    @JacksonXmlProperty(namespace = cbc, localName = "Mail")
    val mail: String?
)

data class PayeeFinancialAccount(
    @JacksonXmlProperty(namespace = cbc, localName = "ID")
    val id: String,
    @JacksonXmlProperty(namespace = cbc, localName = "Name")
    val name: String
)
