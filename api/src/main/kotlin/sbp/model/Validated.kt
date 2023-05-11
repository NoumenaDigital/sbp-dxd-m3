package sbp.model

import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import java.time.Period
import java.time.format.DateTimeParseException

data class ValidationErrors(val errors: MutableList<ValidationError>)
data class ValidationError(val field: String, val value: Any?, val message: String)

val errorsLens = Body.auto<ValidationErrors>().toLens()

data class ValidationException(
    override val message: String? = null,
    override val cause: Throwable? = null,
    val errors: ValidationErrors
) : RuntimeException()

fun isValidPeriod(period: String): Boolean {
    var isPeriod = true
    try {
        Period.parse(period)
    } catch (ex: DateTimeParseException) {
        isPeriod = false
    }
    return isPeriod
}
