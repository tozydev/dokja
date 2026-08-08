package vn.id.tozydev.dokja.backend

import kotlin.test.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class ModularityTests {
    private var modules = ApplicationModules.of(DokjaBackendApplication::class.java)

    @Test
    fun `verifies modular structure`() {
        modules.verify()
    }

    @Test
    fun `create module documentation`() {
        Documenter(modules).writeDocumentation()
    }
}
