package br.com.zupacademy.thiago.pix.validation

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidUUIDValidator::class])
annotation class ValidUUID(
    val message: String = "UUID inválido",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidUUIDValidator: ConstraintValidator<ValidUUID, String> {

    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<ValidUUID>,
        context: ConstraintValidatorContext?
    ): Boolean {
        if (value == null){
            return true
        }

        return value.matches("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b".toRegex())
    }
}