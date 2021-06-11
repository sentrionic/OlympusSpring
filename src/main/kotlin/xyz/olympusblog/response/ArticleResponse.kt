package xyz.olympusblog.response

data class ArticleListResponse(
    val articles: List<ArticleResponse>,
    val hasMore: Boolean
)

data class ArticleResponse(
    val id: Int,
    val slug: String,
    val title: String,
    val description: String,
    val body: String,
    val image: String,
    val tagList: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val favorited: Boolean = false,
    val bookmarked: Boolean = false,
    val favoritesCount: Int = 0,
    val author: Profile
)