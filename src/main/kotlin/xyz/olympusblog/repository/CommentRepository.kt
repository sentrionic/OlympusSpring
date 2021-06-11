package xyz.olympusblog.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import xyz.olympusblog.models.Article
import xyz.olympusblog.models.Comment

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByArticle(article: Article): List<Comment>
    fun findByArticleOrderByCreatedAtDesc(article: Article): List<Comment>
}