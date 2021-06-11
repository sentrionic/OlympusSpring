package xyz.olympusblog.controller

import xyz.olympusblog.dto.user.LoginDTO
import xyz.olympusblog.dto.user.RegisterDTO
import xyz.olympusblog.models.User
import xyz.olympusblog.service.AuthService

import org.springframework.validation.FieldError


import org.springframework.web.bind.MethodArgumentNotValidException

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import xyz.olympusblog.dto.password.ChangePasswordDTO
import xyz.olympusblog.dto.password.ForgotPasswordDTO
import xyz.olympusblog.dto.password.ResetPasswordDTO
import xyz.olympusblog.dto.user.UpdateUserDTO
import xyz.olympusblog.models.ValidationErrors
import java.io.File
import java.util.function.Consumer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull


@RestController
@RequestMapping("/api")
class AuthController(private val authService: AuthService) {

    private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
    private val validator: Validator = factory.validator

    @PostMapping("/users")
    fun register(@Valid @RequestBody input: RegisterDTO): User {
        return authService.register(input)
    }

    @PostMapping("/users/login")
    fun login(@RequestBody input: LoginDTO): User {
        return authService.login(input)
    }

    @PostMapping("/users/logout")
    fun login(request: HttpServletRequest, response: HttpServletResponse): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null) SecurityContextLogoutHandler().logout(request, response, authentication)
        return true
    }

    @GetMapping("/user")
    fun getUser(): User {
        return authService.getCurrentUser()
    }

    @PutMapping("/user")
    fun updateUser(
        @RequestParam username: String,
        @RequestParam email: String,
        @RequestParam(required = false) image: MultipartFile?,
        @RequestParam(required = false) bio: String?,
    ): ResponseEntity<Any> {
        val input = UpdateUserDTO(username, email, bio, image)
        val violations: Set<ConstraintViolation<UpdateUserDTO>> = validator.validate(input)
        if (violations.isNotEmpty()) {
            val list = violations.map { violation ->
                ValidationErrors.ValidationError(
                    violation.propertyPath.toString(),
                    violation.message
                )
            }
            return ResponseEntity(ValidationErrors(list), HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(authService.updateUser(input), HttpStatus.OK)
    }

    @PutMapping("/users/change-password")
    fun changePassword(
        @Valid @RequestBody input: ChangePasswordDTO
    ): ResponseEntity<Any> {
        return ResponseEntity(authService.changePassword(input), HttpStatus.OK)
    }

    @PostMapping("/users/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody input: ForgotPasswordDTO
    ): ResponseEntity<Boolean> {
        return ResponseEntity(authService.forgotPassword(input.email), HttpStatus.OK)
    }

    @PostMapping("/users/reset-password")
    fun resetPassword(@Valid @RequestBody input: ResetPasswordDTO): ResponseEntity<User> {
        return ResponseEntity(authService.resetPassword(input), HttpStatus.OK)
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