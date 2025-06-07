package meli.integrador.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import meli.integrador.annotations.Password;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    Pattern pattern = Pattern.compile("^(?=.*[a-z])" +
            "(?=.*[A-Z])" +
            "(?=.*\\d)" +
            "(?=.*[@$!%*?&])" +
            "[A-Za-z\\d@$!%*?&]{8,}$");

    @Override
    public void initialize(Password constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return password != null && pattern.asPredicate().test(password);
    }
}
