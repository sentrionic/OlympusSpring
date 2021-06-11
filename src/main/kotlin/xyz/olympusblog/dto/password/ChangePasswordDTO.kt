package xyz.olympusblog.dto.password

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@PasswordsEqualConstraint(message = "passwords are not equal")
data class ChangePasswordDTO(
    @field:NotNull(message = "Current Password is required")
    val currentPassword: String?,

    @field:NotNull(message = "New Password is required")
    @field:Size(min = 6, max = 150, message = "Password must be between 6 and 150 characters")
    val newPassword: String?,

    @field:NotNull(message = "New Password is required")
    @field:Size(min = 6, max = 150, message = "Password must be between 6 and 150 characters")
    val confirmNewPassword: String?
)