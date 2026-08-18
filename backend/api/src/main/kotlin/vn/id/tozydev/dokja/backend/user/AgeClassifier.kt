package vn.id.tozydev.dokja.backend.user

import java.time.Clock
import java.time.LocalDate
import java.time.Period
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Component

/**
 * Derives [AgeClassification] and age from a birthdate using an injected [Clock].
 *
 * The [Clock] makes age computation deterministic and testable.
 */
@Component
class AgeClassifier(private val clock: Clock) {

    fun classify(birthDate: LocalDate?): AgeClassification {
        if (birthDate == null) {
            return AgeClassification.P
        }
        val age = computeAge(birthDate)
        return when {
            age < 13 -> AgeClassification.P
            age < 16 -> AgeClassification.R_13
            age < 18 -> AgeClassification.R_16
            else -> AgeClassification.R_18
        }
    }

    fun computeAge(birthDate: LocalDate): Int =
        Period.between(birthDate, LocalDate.now(clock)).years

    /**
     * Checks if the current user (from [SecurityContextHolder]) satisfies the [required] age
     * classification. Always returns `true` when [required] is P.
     */
    fun isSatisfied(required: AgeClassification): Boolean {
        val userClassification = classify(extractBirthdateFromContext())
        return userClassification.isAtLeast(required)
    }

    private fun extractBirthdateFromContext(): LocalDate? {
        val user =
            SecurityContextHolder.getContext().authentication?.principal as? OidcUser ?: return null
        return user.userInfo?.birthdate?.let { LocalDate.parse(it) }
    }
}
