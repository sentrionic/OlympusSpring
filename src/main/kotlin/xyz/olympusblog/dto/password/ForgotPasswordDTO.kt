package xyz.olympusblog.dto.password

import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

data class ForgotPasswordDTO(
    @field:Email
    @field:NotEmpty(message = "Email is required")
    val email: String
)