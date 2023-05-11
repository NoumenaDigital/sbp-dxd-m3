package utils

fun correctUblBase64Encoded(invoiceNumber: String): String {
    return """
    <Invoice
        xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
        xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
        xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2">
        <cbc:ID>$invoiceNumber</cbc:ID>
        <cbc:IssueDate>2022-10-25T00:00Z</cbc:IssueDate>
        <cbc:Deadline>2022-10-28T00:00Z</cbc:Deadline>
        <cac:Amount>
            <cbc:Amount>112.50</cbc:Amount>
            <cbc:Unit>EUR</cbc:Unit>
        </cac:Amount>
        <cac:AccountingSupplierParty>
            <cac:Party>
                <cbc:EndpointID schemeID="0106">11</cbc:EndpointID>
                <cbc:IBAN>ZZ0000000000000001</cbc:IBAN>
                <cac:PartyName>
                    <cbc:Name>User 1</cbc:Name>
                    <cbc:Address>User 1, Address 1111</cbc:Address>
                </cac:PartyName>
            </cac:Party>
        </cac:AccountingSupplierParty>
        <cac:AccountingCustomerParty>
            <cac:Party>
                <cac:PartyName>
                    <cbc:Name>Debtor</cbc:Name>
                    <cbc:Address>Debtor 1, Address 11111</cbc:Address>
                </cac:PartyName>
            </cac:Party>
        </cac:AccountingCustomerParty>
        <cbc:FreeTextDescription>free text here</cbc:FreeTextDescription>
    </Invoice>
    """.trimIndent()
}

fun incorrectUblBase64Encoded(): String {
    return """
    <Invoice
        xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
        xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
        xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2">
        <cbc:ID>INV05</cbc:ID>
        <cbc:IssueDate>2022-10-25T00:00Z</cbc:IssueDate>
        <cbc:Deadline>2022-10-28T00:00Z</cbc:Deadline>
        <cac:Amount>
            <cbc:Amount>150</cbc:Amount>
            <cbc:Unit>EUR</cbc:Unit>
        </cac:Amount>
        <cac:AccountingSupplierParty>
            <cac:Party>
                <cbc:EndpointID schemeID="0106">11</cbc:EndpointID>
                <cbc:IBAN>ZZ0000000000000001</cbc:IBAN>
                <cac:PartyName>
                    <cbc:Name>User 1</cbc:Name>
                    <cbc:Address>User 1, Address 1111</cbc:Address>
                    <cbc:Mail>user1@mail.com</cbc:Mail>
                </cac:PartyName>
            </cac:Party>
        </cac:AccountingSupplierParty>
        <cac:AccountingCustomerParty>
            <cac:Party>
                <cac:PartyName>
                    <cbc:Name>Debtor</cbc:Name>
                    <cbc:Address>Debtor 1, Address 11111</cbc:Address>
                </cac:PartyName>
            </cac:Party>
        </cac:AccountingCustomerParty>
        <cbc:FreeTextDescription>free text here</cbc:FreeTextDescription>
    </Invoice>
    """.trimIndent()
}

fun missingFieldsUblBase64Encoded(): String {
    return """
    <Invoice
        xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
        xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
        xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2">
        <cbc:ID>F2209840543</cbc:ID>
        <cbc:IssueDate>2022-10-25T00:00Z</cbc:IssueDate>
        <cbc:Deadline>2022-10-28T00:00Z</cbc:Deadline>
        <cac:Amount>
            <cbc:Amount>112.50</cbc:Amount>
            <cbc:Unit>Euros</cbc:Unit>
        </cac:Amount>
        <cac:AccountingCustomerParty>
            <cac:Party>
                <cac:PartyName>
                    <cbc:Name>Debtor</cbc:Name>
                    <cbc:Address>Debtor 1, Address 11111</cbc:Address>
                </cac:PartyName>
            </cac:Party>
        </cac:AccountingCustomerParty>
        <cac:PaymentMeans>
            <cac:PayeeFinancialAccount>
                <cbc:ID>NL64ABNC0417164300</cbc:ID>
                <cbc:Name>User 1</cbc:Name>
            </cac:PayeeFinancialAccount>
        </cac:PaymentMeans>
    </Invoice>
    """.trimIndent()
}
