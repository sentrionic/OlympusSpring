package xyz.olympusblog.dto.password

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class ResetPasswordDTO(
    @field:NotNull(message = "Token is required")
    val token: String?,

    @field:NotNull(message = "New Password is required")
    @field:Size(min = 6, max = 150, message = "Password must be between 6 and 150 characters")
    val newPassword: String?,

    @field:NotNull(message = "New Password is required")
    @field:Size(min = 6, max = 150, message = "Password must be between 6 and 150 characters")
    val confirmNewPassword: String?
)