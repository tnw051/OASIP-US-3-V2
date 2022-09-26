package int221.oasip.backendus3.validators;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * The annotated field must be parsable to the specified enum by the specified method.
 * <br/>
 * The method must take a single String argument and return an enum.
 * <br/>
 * <p>
 * {@code null} elements are considered invalid.
 *
 * @author Tawan Muadmuenwai
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValue.EnumValueValidator.class)
public @interface EnumValue {
    Class<? extends Enum<?>> enumClass();

    String method();

    String message() default "must be any of enum {enumClass}";

    Class<?>[] groups() default {};

    Class<? extends javax.validation.Payload>[] payload() default {};

    class EnumValueValidator implements ConstraintValidator<EnumValue, String> {
        private Method method;

        @Override
        public void initialize(EnumValue constraintAnnotation) {
            try {
                String methodName = constraintAnnotation.method();
                this.method = constraintAnnotation.enumClass().getMethod(methodName, String.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null) {
                return false;
            }

            try {
                Object obj = method.invoke(null, value);
                return obj != null;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
