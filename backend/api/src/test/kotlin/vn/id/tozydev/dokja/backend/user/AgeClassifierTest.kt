package vn.id.tozydev.dokja.backend.user

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AgeClassifierTest {

    private val fixedInstant = Instant.parse("2026-08-18T00:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    private val classifier = AgeClassifier(clock)
    private val today = LocalDate.now(clock)

    @Test
    fun `classify child under 13 as P`() {
        val birthDate = today.minusYears(10)
        assertThat(classifier.classify(birthDate)).isEqualTo(AgeClassification.P)
    }

    @Test
    fun `classify age 13 as R_13`() {
        val birthDate = today.minusYears(13)
        assertThat(classifier.classify(birthDate)).isEqualTo(AgeClassification.R_13)
    }

    @Test
    fun `classify age 15 as R_13`() {
        val birthDate = today.minusYears(15)
        assertThat(classifier.classify(birthDate)).isEqualTo(AgeClassification.R_13)
    }

    @Test
    fun `classify age 16 as R_16`() {
        val birthDate = today.minusYears(16)
        assertThat(classifier.classify(birthDate)).isEqualTo(AgeClassification.R_16)
    }

    @Test
    fun `classify age 17 as R_16`() {
        val birthDate = today.minusYears(17)
        assertThat(classifier.classify(birthDate)).isEqualTo(AgeClassification.R_16)
    }

    @Test
    fun `classify age 18 as R_18`() {
        val birthDate = today.minusYears(18)
        assertThat(classifier.classify(birthDate)).isEqualTo(AgeClassification.R_18)
    }

    @Test
    fun `classify age 30 as R_18`() {
        val birthDate = today.minusYears(30)
        assertThat(classifier.classify(birthDate)).isEqualTo(AgeClassification.R_18)
    }

    @Test
    fun `computeAge returns correct age when birthday is in the future`() {
        val birthDate = today.minusYears(25).plusDays(1)
        assertThat(classifier.computeAge(birthDate)).isEqualTo(24)
    }

    @Test
    fun `classify null birthdate as P`() {
        assertThat(classifier.classify(null)).isEqualTo(AgeClassification.P)
    }

    @Test
    fun `computeAge returns exact age on birthday`() {
        val birthDate = today.minusYears(20)
        assertThat(classifier.computeAge(birthDate)).isEqualTo(20)
    }
}
