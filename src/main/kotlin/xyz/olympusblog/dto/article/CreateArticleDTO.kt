package xyz.olympusblog.dto.article

import org.springframework.web.multipart.MultipartFile
import java.io.File
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class CreateArticleDTO(
    @field:NotEmpty
    @field:Size(min = 10, max = 100, message = "Title must be between 10 and 100 characters")
    val title: String,

    @field:NotEmpty
    @field:Size(min = 10, max = 150, message = "Description must be between 10 and 100 characters")
    val description: String,

    @field:NotEmpty(message = "Body must not be empty")
    val body: String,

    val image: MultipartFile? = null,

    @field:NotNull
    @field:Size(min = 1, max = 5)
    val tagList: List<@NotNull @Size(min = 3, max = 15) String>
)