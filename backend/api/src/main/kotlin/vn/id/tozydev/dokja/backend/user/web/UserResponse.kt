package vn.id.tozydev.dokja.backend.user.web

import java.time.LocalDate
import vn.id.tozydev.dokja.backend.user.AgeClassification

data class UserResponse(
    val sub: String,
    val name: String?,
    val givenName: String?,
    val familyName: String?,
    val preferredUsername: String?,
    val email: String?,
    val emailVerified: Boolean?,
    val birthdate: LocalDate?,
    val age: Int?,
    val ageClassification: AgeClassification,
)
