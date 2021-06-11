package xyz.olympusblog.dto.comment

import javax.validation.constraints.Max
import javax.validation.constraints.NotNull

data class CommentDTO(
    @field:NotNull
    @Max(250)
    val body: String
)