package casper

import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

fun casperRoutes(client: ContractClient): RoutingHttpHandler {
    val casperHandler = CasperHandler(client)

    return routes(
        "/sbp/api/v1/casper" bind routes(
            "/addDelegate" bind Method.POST to casperHandler.addDelegate()
        )
    )
}
