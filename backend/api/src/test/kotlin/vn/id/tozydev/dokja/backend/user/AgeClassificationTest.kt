package vn.id.tozydev.dokja.backend.user

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AgeClassificationTest {
    @Test
    fun `isAtLeast returns true when user classification meets required`() {
        assertTrue(AgeClassification.P.isAtLeast(AgeClassification.P))
        assertTrue(AgeClassification.R_13.isAtLeast(AgeClassification.P))
        assertTrue(AgeClassification.R_16.isAtLeast(AgeClassification.R_13))
        assertTrue(AgeClassification.R_18.isAtLeast(AgeClassification.R_16))
    }

    @Test
    fun `isAtLeast returns false when user classification is below required`() {
        assertFalse(AgeClassification.P.isAtLeast(AgeClassification.R_13))
        assertFalse(AgeClassification.R_13.isAtLeast(AgeClassification.R_16))
        assertFalse(AgeClassification.R_16.isAtLeast(AgeClassification.R_18))
    }
}
