package xyz.olympusblog.dto.user

import javax.validation.constraints.*

data class RegisterDTO(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String?,

    @field:Email
    @field:NotEmpty(message = "Email is required")
    val email: String?,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 150, message = "Password must be between 6 and 150 characters")
    val password: String?
)