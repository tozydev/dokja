package vn.id.tozydev.dokja.backend.audit

import java.util.Optional
import kotlin.test.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuditorAwareTest {

    private val auditorAware = JwtAuditorAware()

    @AfterEach
    fun cleanUp() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `returns jwt subject for authenticated principal`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("user-123", null, emptyList())

        assertEquals(Optional.of("user-123"), auditorAware.currentAuditor)
    }

    @Test
    fun `returns system actor when no authentication is available`() {
        assertEquals(Optional.of(SYSTEM_ACTOR), auditorAware.currentAuditor)
    }
}
