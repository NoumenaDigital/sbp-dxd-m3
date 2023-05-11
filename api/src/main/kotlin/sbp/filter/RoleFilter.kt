package sbp.filter

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Status
import seed.filter.ErrorCode
import seed.filter.errorResponse
import seed.security.ForwardAuthorization

class RoleFilter(
    private val forwardAuth: ForwardAuthorization,
    private val allowedRoles: List<String>,
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler =
        {
            val party = forwardAuth.party(it)
            val roles = party.access.getValue("party")

            if (!roles.containsAll(allowedRoles)) {
                errorResponse(Status.UNAUTHORIZED, ErrorCode.InvalidRole)
            }

            next(it)
        }
}
