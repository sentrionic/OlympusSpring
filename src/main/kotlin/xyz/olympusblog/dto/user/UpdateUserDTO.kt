package xyz.olympusblog.dto.user

import org.springframework.web.multipart.MultipartFile
import java.io.File
import javax.validation.constraints.*

data class UpdateUserDTO(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String?,

    @field:Email
    @field:NotEmpty(message = "Email is required")
    val email: String?,

    @Max(250, message = "At most 250 characters")
    val bio: String?,

    val image: MultipartFile?
)