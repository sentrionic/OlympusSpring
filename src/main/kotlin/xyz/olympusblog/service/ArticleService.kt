package xyz.olympusblog.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import xyz.olympusblog.dto.article.CreateArticleDTO
import xyz.olympusblog.dto.article.UpdateArticleDTO
import xyz.olympusblog.exception.ArticleNotFoundException
import xyz.olympusblog.exception.SpringRestException
import xyz.olympusblog.exception.UnauthorizedException
import xyz.olympusblog.mapper.ArticleMapper
import xyz.olympusblog.mapper.ProfileMapper
import xyz.olympusblog.models.Article
import xyz.olympusblog.models.Tag
import xyz.olympusblog.models.User
import xyz.olympusblog.repository.ArticleRepository
import xyz.olympusblog.repository.CommentRepository
import xyz.olympusblog.repository.TagRepository
import xyz.olympusblog.repository.UserRepository
import xyz.olympusblog.repository.specification.ArticlesSpecifications
import xyz.olympusblog.response.ArticleListResponse
import xyz.olympusblog.response.ArticleResponse
import java.text.DateFormat
import java.util.*
import javax.transaction.Transactional
import kotlin.random.Random
import java.text.SimpleDateFormat

@Service
@Transactional
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val commentRepository: CommentRepository,
    private val authService: AuthService,
    private val imageUploadService: ImageUploadService,
    private val articleMapper: ArticleMapper,
    private val profileMapper: ProfileMapper,
) {

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    @Transactional
    fun getArticleBySlug(slug: String): ArticleResponse {
        val article = articleRepository.findBySlug(slug) ?: throw ArticleNotFoundException(slug)
        val user = getCurrentUser()
        val author = profileMapper.mapUserToProfile(article.author, user)
        return articleMapper.mapArticleToDto(article, author, user)
    }

    @Transactional
    fun getAllArticles(
        limit: Int = 10,
        page: Int = 0,
        search: String? = "",
        order: String? = "DESC",
        tag: String? = null,
        authorName: String? = null,
        favoritedBy: String? = null,
    ): ArticleListResponse {

        val skip = (page - 1).coerceAtLeast(0)
        val sort = if (order == "ASC") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(skip, limit, sort, "createdAt")

        var articles: Page<Article>

        if (order == "TOP") {
            articles = articleRepository.findAllOrderByFavoritesCountDesc(
                ArticlesSpecifications.queryArticles(
                    if (tag != null) tagRepository.findByName(tag) else null,
                    if (authorName != null) userRepository.findByUsername(authorName) else null,
                    if (favoritedBy != null) userRepository.findByUsername(favoritedBy) else null
                ).and(ArticlesSpecifications.searchArticle(search)),
                pageable
            )
        } else {
            articles = articleRepository.findAll(
                ArticlesSpecifications.queryArticles(
                    if (tag != null) tagRepository.findByName(tag) else null,
                    if (authorName != null) userRepository.findByUsername(authorName) else null,
                    if (favoritedBy != null) userRepository.findByUsername(favoritedBy) else null
                ).and(ArticlesSpecifications.searchArticle(search)),
                pageable
            )

        }

        val user = getCurrentUser()

        val results = articles.toList().map { article ->
            val author = profileMapper.mapUserToProfile(article.author, user)
            articleMapper.mapArticleToDto(article, author, user)
        }

        return ArticleListResponse(
            articles = results,
            hasMore = articles.number + 1 < articles.totalPages
        )
    }

    fun getFeed(
        limit: Int = 10,
        page: Int = 0,
        cursor: String? = null,
    ): ArticleListResponse {
        val skip = (page - 1).coerceAtLeast(0)
        val pageable = PageRequest.of(skip, limit)
        val user = getCurrentUser() ?: throw SpringRestException("Error")

        val date = if (cursor != null) sdf.parse(cursor) else null

        val articles = articleRepository.findFollowingArticles(
            user.id,
            date,
            pageable
        )

        val results = articles.toList().map { article ->
            val author = profileMapper.mapUserToProfile(article.author, user)
            articleMapper.mapArticleToDto(article, author, user)
        }

        return ArticleListResponse(
            articles = results,
            hasMore = articles.number + 1 < articles.totalPages
        )
    }

    fun getBookmarked(
        limit: Int = 10,
        page: Int = 0,
        cursor: String? = null,
    ): ArticleListResponse {
        val skip = (page - 1).coerceAtLeast(0)
        val pageable = PageRequest.of(skip, limit)
        val user = getCurrentUser() ?: throw SpringRestException("Error")
        val date = if (cursor != null) sdf.parse(cursor) else null

        val articles = articleRepository.findByBookmarksOrderByCreatedAtDesc(
            user.id,
            date,
            pageable
        )

        val results = articles.toList().map { article ->
            val author = profileMapper.mapUserToProfile(article.author, user)
            articleMapper.mapArticleToDto(article, author, user)
        }

        return ArticleListResponse(
            articles = results,
            hasMore = articles.number + 1 < articles.totalPages
        )
    }

    fun createArticle(input: CreateArticleDTO): ArticleResponse {
        var url = "https://picsum.photos/seed/${getRandomString(12)}/1080"
        val user = authService.getCurrentUser()

        if (input.image != null) {
            val directory = "spring/${user.id}/${getRandomString(16)}"
            url = imageUploadService.uploadArticleImage(input.image, directory)
        }

        val tagList = input.tagList.map { name ->
            tagRepository.findByName(name) ?: tagRepository.save(Tag(name = name))
        }

        val article = Article(
            title = input.title,
            slug = generateSlug(input.title),
            description = input.description,
            body = input.body,
            image = url,
            tagList = tagList.toMutableList(),
            author = user
        )
        articleRepository.save(article)
        val author = profileMapper.mapUserToProfile(article.author)
        return articleMapper.mapArticleToDto(article, author, user)
    }

    fun favoriteArticle(slug: String): ArticleResponse {
        val article = findArticleForSlug(slug)
        val user = getCurrentUser()

        if (user != null && !article.favorites.contains(user)) {
            article.favorites.add(user)
            articleRepository.save(article)
        }

        val author = profileMapper.mapUserToProfile(article.author, user)
        return articleMapper.mapArticleToDto(article, author, user)
    }

    fun unfavoriteArticle(slug: String): ArticleResponse {
        val article = findArticleForSlug(slug)
        val user = getCurrentUser()

        if (user != null && article.favorites.contains(user)) {
            article.favorites.remove(user)
            articleRepository.save(article)
        }

        val author = profileMapper.mapUserToProfile(article.author, user)
        return articleMapper.mapArticleToDto(article, author, user)
    }

    fun bookmarkArticle(slug: String): ArticleResponse {
        val article = findArticleForSlug(slug)
        val user = getCurrentUser()

        if (user != null && !article.bookmarks.contains(user)) {
            article.bookmarks.add(user)
            articleRepository.save(article)
        }

        val author = profileMapper.mapUserToProfile(article.author, user)
        return articleMapper.mapArticleToDto(article, author, user)
    }

    fun unbookmarkArticle(slug: String): ArticleResponse {
        val article = findArticleForSlug(slug)
        val user = getCurrentUser()

        if (user != null && article.bookmarks.contains(user)) {
            article.bookmarks.remove(user)
            articleRepository.save(article)
        }

        val author = profileMapper.mapUserToProfile(article.author, user)
        return articleMapper.mapArticleToDto(article, author, user)
    }

    fun updateArticle(slug: String, input: UpdateArticleDTO): ArticleResponse {

        val article = findArticleForSlug(slug)
        val user = getCurrentUser()

        if (article.author != user) throw UnauthorizedException()

        var tagList = input.tagList?.map { name ->
            tagRepository.findByName(name) ?: tagRepository.save(Tag(name = name))
        } ?: article.tagList

        article.apply {
            title = input.title ?: title
            description = input.description ?: description
            body = input.body ?: body
            tagList = tagList.toMutableList()
        }

        articleRepository.save(article)
        val author = profileMapper.mapUserToProfile(article.author, user)
        return articleMapper.mapArticleToDto(article, author, user)
    }

    @Transactional
    fun deleteArticle(slug: String): ArticleResponse {
        val article = findArticleForSlug(slug)
        val user = getCurrentUser()

        if (article.author != user) throw UnauthorizedException()

        commentRepository.deleteAll(commentRepository.findByArticle(article))
        articleRepository.delete(article)

        val author = profileMapper.mapUserToProfile(article.author, user)
        return articleMapper.mapArticleToDto(article, author, user)
    }

    fun getTags(): List<String> {
        return tagRepository.findAll(PageRequest.of(0, 10)).toList().map { tag -> tag.name }
    }

    private fun findArticleForSlug(slug: String): Article {
        return articleRepository.findBySlug(slug) ?: throw ArticleNotFoundException(slug)
    }

    @Transactional
    fun getCurrentUser(): User? {
        val username = SecurityContextHolder.getContext().authentication.name
        return userRepository.findByUsername(username)
    }

    private fun generateSlug(title: String): String {
        return title.toLowerCase()
            .replace("\n", " ")
            .replace("[^a-z\\d\\s]".toRegex(), " ")
            .split(" ")
            .joinToString("-")
            .replace("-+".toRegex(), "-")
            .plus("-")
            .plus(getRandomString(6))
    }

    private fun getRandomString(size: Int): String {
        return (1..size)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
}