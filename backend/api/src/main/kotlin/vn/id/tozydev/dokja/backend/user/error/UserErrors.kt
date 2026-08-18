package vn.id.tozydev.dokja.backend.user.error

import org.springframework.http.HttpStatus
import vn.id.tozydev.dokja.backend.error.DomainErrorCode
import vn.id.tozydev.dokja.backend.error.DomainException

data object KeycloakUnavailable : DomainErrorCode

class KeycloakUnavailableException :
    DomainException(
        errorCode = KeycloakUnavailable,
        status = HttpStatus.SERVICE_UNAVAILABLE,
        title = "Service Unavailable",
        detail = "Unable to fetch user profile from identity provider",
    )
