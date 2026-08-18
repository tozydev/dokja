package vn.id.tozydev.dokja.backend.user

import java.time.Clock
import java.time.LocalDate
import java.time.Period
import org.springframework.stereotype.Component

/**
 * Derives [AgeClassification] and age from a birthdate using an injected [Clock].
 *
 * The [Clock] makes age computation deterministic and testable.
 */
@Component
class AgeClassifier(private val clock: Clock) {

    fun classify(birthDate: LocalDate?): AgeClassification {
        if (birthDate == null) return AgeClassification.P
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
}
