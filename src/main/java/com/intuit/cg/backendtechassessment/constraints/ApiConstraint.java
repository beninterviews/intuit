package com.intuit.cg.backendtechassessment.constraints;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.intuit.cg.backendtechassessment.validators.ApiValidator;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.ws.rs.WebApplicationException;
import lombok.RequiredArgsConstructor;

@Target({PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ApiConstraint.Validator.class)
public @interface ApiConstraint {
    /**
     * The class used to validate the constraint.
     * The name is 'value' so it can be passed as a no-name parameter in the annotation
     * \@ApiConstraint(XXX.class) instead of @ApiConstraint(classValidator = XXX.class).
     *
     * @return The class used to validate the constraint.
     */
    Class<? extends ApiValidator> value();

    /**
     * Default message for the constraint.
     *
     * @return The default message for the constraint.
     */
    String message() default "unused";

    /**
     * Required by validation runtime
     *
     * @return Nothing
     */
    Class<?>[] groups() default {};

    /**
     * Required by validation runtime
     *
     * @return Nothing
     */
    Class<? extends Payload>[] payload() default {};

    @RequiredArgsConstructor(onConstructor = @__(@Inject))
    class Validator implements ConstraintValidator<ApiConstraint, Object> {
        private final Injector injector;

        private ApiValidator validator;

        @Override
        public void initialize(ApiConstraint constraint) {
            validator = injector.getInstance(constraint.value());
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean isValid(Object data, ConstraintValidatorContext constraintValidatorContext) {
            try {
                validator.validate(data);
                return true;
            } catch (WebApplicationException exception) {
                constraintValidatorContext.disableDefaultConstraintViolation();

                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(exception.getMessage())
                        .addConstraintViolation();

                return false;
            }
        }
    }
}