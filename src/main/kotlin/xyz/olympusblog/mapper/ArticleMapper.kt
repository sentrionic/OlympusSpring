package xyz.olympusblog.mapper

import org.springframework.stereotype.Service
import xyz.olympusblog.models.Article
import xyz.olympusblog.models.User
import xyz.olympusblog.response.ArticleResponse
import xyz.olympusblog.response.Profile

@Service
class ArticleMapper {

    fun mapArticleToDto(article: Article, author: Profile, currentUser: User?): ArticleResponse {
        return ArticleResponse(
            id = article.id.toInt(),
            slug = article.slug,
            title = article.title,
            description = article.description,
            body = article.body,
            tagList = article.tagList.map { tag -> tag.name },
            image = article.image,
            favorited = article.favorites.contains(currentUser),
            bookmarked = article.bookmarks.contains(currentUser),
            favoritesCount = article.favorites.count(),
            createdAt = article.createdAt.toString(),
            updatedAt = article.updatedAt.toString(),
            author = author
        )
    }
}