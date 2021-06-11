package xyz.olympusblog.dto.password

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [PasswordsEqualConstraintValidator::class])
annotation class PasswordsEqualConstraint(
    val message: String,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class PasswordsEqualConstraintValidator :
    ConstraintValidator<PasswordsEqualConstraint?, Any> {
    override fun initialize(arg0: PasswordsEqualConstraint?) {}
    override fun isValid(candidate: Any, arg1: ConstraintValidatorContext): Boolean {
        val user: ChangePasswordDTO = candidate as ChangePasswordDTO
        return user.newPassword == user.confirmNewPassword
    }
}