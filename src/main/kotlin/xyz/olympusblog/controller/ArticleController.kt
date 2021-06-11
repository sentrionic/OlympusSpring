package xyz.olympusblog.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import xyz.olympusblog.dto.article.CreateArticleDTO
import xyz.olympusblog.dto.article.UpdateArticleDTO
import xyz.olympusblog.models.ValidationErrors
import xyz.olympusblog.response.ArticleListResponse
import xyz.olympusblog.response.ArticleResponse
import xyz.olympusblog.service.ArticleService
import java.io.File
import java.util.function.Consumer
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory
import javax.validation.ConstraintViolation

@RestController
@RequestMapping("/api/articles")
class ArticleController(private val articleService: ArticleService) {

    private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
    private val validator: Validator = factory.validator

    @PostMapping
    fun createArticle(
        @RequestParam title: String,
        @RequestParam description: String,
        @RequestParam(required = false) image: MultipartFile?,
        @RequestParam body: String,
        @RequestParam(value = "tagList[]") tagList: List<String>
    ): ResponseEntity<Any> {
        val input = CreateArticleDTO(title, description, body, image, tagList)
        val violations: Set<ConstraintViolation<CreateArticleDTO>> = validator.validate(input)
        if (violations.isNotEmpty()) {
            val list = violations.map { violation ->
                ValidationErrors.ValidationError(
                    violation.propertyPath.toString(),
                    violation.message
                )
            }
            return ResponseEntity(ValidationErrors(list), HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(articleService.createArticle(input), HttpStatus.CREATED)
    }

    @PutMapping("/{slug}")
    fun updateArticle(
        @PathVariable(required = false) slug: String,
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) image: MultipartFile?,
        @RequestParam(required = false) body: String?,
        @RequestParam(value = "tagList[]", required = false) tagList: List<String>?
    ): ResponseEntity<Any> {
        val input = UpdateArticleDTO(title, description, body, image, tagList)
        val violations: Set<ConstraintViolation<UpdateArticleDTO>> = validator.validate(input)
        if (violations.isNotEmpty()) {
            val list = violations.map { violation ->
                ValidationErrors.ValidationError(
                    violation.propertyPath.toString(),
                    violation.message
                )
            }
            return ResponseEntity(ValidationErrors(list), HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(articleService.updateArticle(slug, input), HttpStatus.CREATED)
    }

    @GetMapping
    fun getAllArticles(
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @RequestParam(required = false, defaultValue = "0") p: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) tag: String?,
        @RequestParam(required = false) author: String?,
        @RequestParam(required = false) favorited: String?,
        @RequestParam(required = false) order: String?,
    ): ResponseEntity<ArticleListResponse> {
        return ResponseEntity(articleService.getAllArticles(
            limit = limit,
            page = p,
            search = search,
            tag = tag,
            authorName = author,
            favoritedBy = favorited,
            order = order
        ), HttpStatus.OK)
    }

    @GetMapping("/feed")
    fun getFeed(
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @RequestParam(required = false, defaultValue = "0") p: Int,
        @RequestParam(required = false) cursor: String?,
    ): ResponseEntity<ArticleListResponse> {
        return ResponseEntity(articleService.getFeed(limit, p, cursor), HttpStatus.OK)
    }

    @GetMapping("/bookmarked")
    fun getBookmarked(
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @RequestParam(required = false, defaultValue = "0") p: Int,
        @RequestParam(required = false) cursor: String?,
    ): ResponseEntity<ArticleListResponse> {
        return ResponseEntity(articleService.getBookmarked(limit, p, cursor), HttpStatus.OK)
    }

    @GetMapping("/tags")
    fun getTags(
    ): ResponseEntity<List<String>> {
        return ResponseEntity(articleService.getTags(), HttpStatus.OK)
    }

    @GetMapping("/{slug}")
    fun getArticle(@PathVariable slug: String): ResponseEntity<ArticleResponse> {
        return status(HttpStatus.OK).body(articleService.getArticleBySlug(slug))
    }

    @PostMapping("/{slug}/favorite")
    fun favoriteArticle(@PathVariable slug: String): ArticleResponse {
        return articleService.favoriteArticle(slug)
    }

    @DeleteMapping("/{slug}/favorite")
    fun unfavoriteArticle(@PathVariable slug: String): ArticleResponse {
        return articleService.unfavoriteArticle(slug)
    }

    @PostMapping("/{slug}/bookmark")
    fun bookmarkArticle(@PathVariable slug: String): ArticleResponse {
        return articleService.bookmarkArticle(slug)
    }

    @DeleteMapping("/{slug}/bookmark")
    fun unbookmarkArticle(@PathVariable slug: String): ArticleResponse {
        return articleService.unbookmarkArticle(slug)
    }

    @DeleteMapping("/{slug}")
    fun deleteArticle(@PathVariable slug: String): ArticleResponse {
        return articleService.deleteArticle(slug)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException
    ): ValidationErrors {
        val errors = mutableListOf<ValidationErrors.ValidationError>()
        ex.bindingResult.allErrors.forEach(Consumer { error: ObjectError ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.getDefaultMessage()
            errors.add(ValidationErrors.ValidationError(fieldName, errorMessage.toString()))
        })
        return ValidationErrors(errors)
    }
}