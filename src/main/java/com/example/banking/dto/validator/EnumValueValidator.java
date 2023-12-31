package com.example.banking.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.List;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

    private List<String> enumValues;

    @Override
    public void initialize(@NonNull EnumValue constraintAnnotation) {

        Class<?> cls = constraintAnnotation.enumClass();

        enumValues = Arrays.stream(cls.getEnumConstants())
                .map(obj -> (Enum<?>) obj)
                .map(Enum::name)
                .toList();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || enumValues.contains(value);
    }

}
