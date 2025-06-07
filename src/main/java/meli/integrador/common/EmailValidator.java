package meli.integrador.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import meli.integrador.annotations.Email;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<Email, String> {

    static Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public void initialize(Email constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return email != null && pattern.asPredicate().test(email);
    }
}