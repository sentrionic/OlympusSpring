package xyz.olympusblog.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import xyz.olympusblog.dto.comment.CommentDTO
import xyz.olympusblog.exception.ArticleNotFoundException
import xyz.olympusblog.exception.CommentNotFoundException
import xyz.olympusblog.exception.UnauthorizedException
import xyz.olympusblog.mapper.ProfileMapper
import xyz.olympusblog.models.Comment
import xyz.olympusblog.repository.ArticleRepository
import xyz.olympusblog.repository.CommentRepository
import xyz.olympusblog.repository.UserRepository
import xyz.olympusblog.response.CommentResponse
import javax.transaction.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository,
    private val profileMapper: ProfileMapper,
    private val authService: AuthService,
) {

    @Transactional
    fun createComment(slug: String, input: CommentDTO): CommentResponse {
        val article = articleRepository.findBySlug(slug)?: throw ArticleNotFoundException("Article with slug $slug not found")
        val user = authService.getCurrentUser()

        val comment = Comment(
            body = input.body,
            author = user,
            article = article
        )
        commentRepository.save(comment)

        return CommentResponse(
            body = comment.body,
            createdAt = comment.createdAt.toString(),
            updatedAt = comment.updatedAt.toString(),
            id = comment.id.toInt(),
            author = profileMapper.mapUserToProfile(user),
        )
    }

    @Transactional
    fun getCommentsBySlug(slug: String): List<CommentResponse> {
        val article = articleRepository.findBySlug(slug)?: throw ArticleNotFoundException("Article with slug $slug not found")
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)

        val comments = commentRepository.findByArticle(article)
        return comments.map { comment ->
            CommentResponse(
                body = comment.body,
                createdAt = comment.createdAt.toString(),
                updatedAt = comment.updatedAt.toString(),
                id = comment.id.toInt(),
                author = profileMapper.mapUserToProfile(comment.author, user),
            )
        }
    }

    @Transactional
    fun deleteComment(slug: String, commentId: Int): CommentResponse {
        val article = articleRepository.findBySlug(slug)?: throw ArticleNotFoundException("Article with slug $slug not found")
        val comment = commentRepository.findById(commentId.toLong()).orElseThrow { throw CommentNotFoundException("Comment with ID $commentId not found") }

        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)

        if (comment.author != user) throw UnauthorizedException()

        commentRepository.delete(comment)
        return CommentResponse(
            body = comment.body,
            createdAt = comment.createdAt.toString(),
            updatedAt = comment.updatedAt.toString(),
            id = comment.id.toInt(),
            author = profileMapper.mapUserToProfile(comment.author, user),
        )
    }
}