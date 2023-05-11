package sbp.model

import arrow.core.getOrHandle
import com.noumenadigital.npl.api.generated.sbp.InvoiceFacade
import com.noumenadigital.npl.api.generated.sbp.MilestoneFacade
import com.noumenadigital.platform.engine.client.AuthorizationProvider
import com.noumenadigital.platform.engine.client.EngineClientApi

fun MilestoneFacade.toMilestoneDetails(engineClient: EngineClientApi, auth: AuthorizationProvider) =
    MilestoneDetails(this, this.getBlockchainRef(engineClient, auth))

fun MilestoneFacade.getBlockchainRef(engineClient: EngineClientApi, auth: AuthorizationProvider): String? {
    return fields.originalInvoice?.let { invoice ->
        val invoiceProtocol = engineClient.getProtocolStateById(invoice.id, auth).getOrHandle { throw it }
        val invoiceFacade = InvoiceFacade(invoiceProtocol)
        invoiceFacade.fields.blockchainRef
    }
}
