package xyz.olympusblog.service

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import xyz.olympusblog.dto.password.ChangePasswordDTO
import xyz.olympusblog.dto.password.ResetPasswordDTO
import xyz.olympusblog.dto.user.LoginDTO
import xyz.olympusblog.dto.user.RegisterDTO
import xyz.olympusblog.dto.user.UpdateUserDTO
import xyz.olympusblog.exception.ProfileNotFoundException
import xyz.olympusblog.exception.SpringRestException
import xyz.olympusblog.models.Email
import xyz.olympusblog.models.User
import xyz.olympusblog.repository.UserRepository
import java.security.MessageDigest
import java.util.*
import javax.transaction.Transactional
import kotlin.text.Charsets.UTF_8

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val redis: RedisService,
    private val imageUploadService: ImageUploadService,
    private val emailService: EmailService,
    private val mailContentBuilder: MailContentBuilder
) {

    @Transactional
    fun register(input: RegisterDTO): User {

        var checkUser = userRepository.existsByUsername(input.username!!)
        if (checkUser) {
            throw SpringRestException("Username already taken")
        }

        checkUser = userRepository.existsByUsername(input.email!!)
        if (checkUser) {
            throw SpringRestException("Email already in use")
        }

        val user = User(
            email = input.email,
            username = input.username,
            password = encodePassword(input.password!!),
            image = "https://gravatar.com/avatar/${md5(input.email).toHex()}?d=identicon",
            bio = ""
        )

        userRepository.save(user)

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                input.email,
                input.password
            )
        )
        SecurityContextHolder.getContext().authentication = authentication

        return user
    }

    fun login(input: LoginDTO): User {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                input.email,
                input.password
            )
        )
        SecurityContextHolder.getContext().authentication = authentication

        return getCurrentUser()
    }

    private fun encodePassword(password: String): String {
        return passwordEncoder.encode(password)
    }

    @Transactional
    fun getCurrentUser(): User {
        val username = SecurityContextHolder.getContext().authentication.name
        return userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User name not found - $username")
    }

    fun updateUser(input: UpdateUserDTO): User {
        val user = getCurrentUser()

        if (input.email != user.email) {
            val emailExists = userRepository.existsByEmail(input.email!!)
            if (emailExists) throw SpringRestException("Email already in use")
        }

        if (input.username != user.username) {
            val usernameExists = userRepository.existsByUsername(input.username!!)
            if (usernameExists) throw SpringRestException("Username already in use")
        }

        if (input.image != null) {
            val directory = "spring/${user.id}/avatar"
            user.image = imageUploadService.uploadAvatarImage(input.image, directory)
        }

        user.apply {
            username = input.username
            bio = input.bio ?: bio
            email = input.email
        }
        userRepository.save(user)
        return user
    }

    fun changePassword(input: ChangePasswordDTO): User {
        val user = getCurrentUser()

        user.apply {
            password = passwordEncoder.encode(input.newPassword)
        }

        userRepository.save(user)

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                user.email,
                input.newPassword
            )
        )
        SecurityContextHolder.getContext().authentication = authentication

        return user
    }

    fun forgotPassword(email: String): Boolean {

        val user = userRepository.findByEmail(email)
            ?: throw ProfileNotFoundException("An account with this email does not exist")
        val token = UUID.randomUUID()
        redis.saveUserId(user.id.toString(), token)

        val message =
            mailContentBuilder.build("Click the following link to reset your email: <a href=\"localhost:3000/reset-password/${token}\">Reset Password</a>")

        emailService.sendEmail(
            Email(
                subject = "Reset Password",
                recipient = email,
                body = message
            )
        )

        return true
    }

    fun resetPassword(input: ResetPasswordDTO): User {
        val userId = redis.getUserId(token = input.token!!)

        val user = userRepository.findById(userId).orElseThrow { throw ProfileNotFoundException("Profile Not Found") }

        user.apply {
            password = passwordEncoder.encode(input.newPassword)
        }

        redis.deleteKey(input.token)

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                user.email,
                input.newPassword
            )
        )
        SecurityContextHolder.getContext().authentication = authentication

        return user
    }

    fun md5(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray(UTF_8))
    fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}