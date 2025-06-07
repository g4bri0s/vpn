package meli.integrador.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import meli.integrador.annotations.Name;
import java.util.regex.Pattern;

public class NameValidator implements ConstraintValidator<Name, String> {

    static Pattern pattern = Pattern.compile("^[A-Za-zÀ-ÖØ-öø-ÿ ]+$");

    @Override
    public void initialize(Name constraintAnnotation) {
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return name != null && pattern.asPredicate().test(name);
    }
}