package xyz.olympusblog.controller

import org.springframework.web.bind.annotation.*
import xyz.olympusblog.dto.comment.CommentDTO
import xyz.olympusblog.response.CommentResponse
import xyz.olympusblog.service.CommentService
import javax.validation.Valid

@RestController
@RequestMapping("/api/articles")
class CommentsController(private val commentService: CommentService) {

    @PostMapping("/{slug}/comments")
    fun createComment(@PathVariable slug: String, @Valid @RequestBody input: CommentDTO): CommentResponse {
        return commentService.createComment(slug, input)
    }

    @GetMapping("/{slug}/comments")
    fun getAllComments(@PathVariable slug: String): List<CommentResponse> {
        return commentService.getCommentsBySlug(slug)
    }

    @DeleteMapping("/{slug}/comments/{commentId}")
    fun deleteComment(@PathVariable slug: String, @PathVariable commentId: Int): CommentResponse {
        return commentService.deleteComment(slug, commentId)
    }

}