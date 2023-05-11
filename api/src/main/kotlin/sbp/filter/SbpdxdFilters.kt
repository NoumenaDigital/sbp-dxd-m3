package sbp.filter

import com.noumenadigital.platform.engine.values.ClientException
import com.noumenadigital.platform.engine.values.ClientGuardOrigin
import config.ISbpdxdConfiguration
import mu.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import sbp.model.ErrorResponse
import sbp.model.ValidationException
import sbp.model.errorsLens
import sbp.model.verifiedErrorResponse
import seed.filter.errorFilter

internal val logger = KotlinLogging.logger {}

fun blockedEndpointsFilter(config: ISbpdxdConfiguration) =
    Filter { next ->
        {
            if (config.disableContracts) {
                Response(Status.NOT_IMPLEMENTED)
            } else next(it)
        }
    }

fun catchValidationException() =
    Filter { next ->
        {
            try {
                next(it)
            } catch (e: ValidationException) {
                logger.error { "ValidationException: $e, request: $it" }
                Response(Status.BAD_REQUEST).with(errorsLens of e.errors)
            }
        }
    }

fun catchGuardException() =
    Filter { next ->
        {
            try {
                next(it)
            } catch (e: ClientException.PlatformRuntimeException) {
                when (val origin = e.origin) {
                    is ClientGuardOrigin -> {
                        logger.error { "ClientException.PlatformRuntimeException: ClientGuardOrigin: $e, request: $it" }
                        Response(Status.BAD_REQUEST).with(verifiedErrorResponse of ErrorResponse(origin.message))
                    }

                    else -> throw e
                }
            }
        }
    }

fun sbpdxdErrorFilter(debug: Boolean = false) =
    errorFilter(debug)
        .then(catchValidationException())
        .then(catchGuardException())
