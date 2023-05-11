package casper

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.auto

private val addDelegateCargoLens = Body.auto<AddDelegateCargo>().toLens()

class CasperHandler(var client: ContractClient) {

    fun addDelegate(): HttpHandler {
        return { req ->

            val cargo = addDelegateCargoLens(req)

            val result = client.addDelegate(cargo)

            Response(Status.OK).body(result)
        }
    }
}
