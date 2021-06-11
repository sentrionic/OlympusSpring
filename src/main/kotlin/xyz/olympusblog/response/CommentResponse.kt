package xyz.olympusblog.response

data class CommentResponse(
    val id: Int,
    val body: String,
    val author: Profile,
    val createdAt: String,
    val updatedAt: String
)