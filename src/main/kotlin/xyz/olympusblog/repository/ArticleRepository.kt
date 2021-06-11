package xyz.olympusblog.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Temporal
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import xyz.olympusblog.models.Article
import java.util.*

@Repository
interface ArticleRepository : JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    fun findBySlug(slug: String): Article?

    @Query(
        value = "select a from Article a left join a.favorites order by a.favorites.size desc"
    )
    fun findAllOrderByFavoritesCountDesc(specification: Specification<Article>, pageable: Pageable): Page<Article>

    @Query(
        nativeQuery = true,
        value = """
            SELECT a.*
            FROM articles a left outer join users on a.author_id = users.id left outer join user_followings uf on users.id = uf.user_id
            where uf.followers_id = :id
            AND (DATE(a.created_at) < cast(:cursor AS date) or cast(:cursor as date) is null)
            order by created_at DESC
        """
    )
    fun findFollowingArticles(@Param("id") id: Long, @Param("cursor") @Temporal cursor: Date? = null, pageable: Pageable): Page<Article>

    @Query(
        nativeQuery = true,
        value = """
            SELECT a.*
            FROM articles a FULL OUTER JOIN article_bookmarks ab ON a.id = ab.article_id
            WHERE ab.bookmarks_id = :userId
            AND (DATE(a.created_at) < cast(:cursor AS date) or cast(:cursor as date) is null)
            order by created_at DESC
        """
    )
    fun findByBookmarksOrderByCreatedAtDesc(@Param("userId") userId: Long, @Param("cursor") @Temporal cursor: Date? = null, pageable: Pageable): Page<Article>
}