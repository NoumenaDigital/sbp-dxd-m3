package sbp.model

import org.http4k.core.Body
import org.http4k.format.Jackson.auto

data class ErrorResponse(
    val message: String
)

val verifiedErrorResponse = Body.auto<ErrorResponse>().toLens()
