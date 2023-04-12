package ru.yandex.practicum.filmorate.ValidationTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Director;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DirectorValidationTest {
    static Validator validator;

    @BeforeAll
    public static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createFilmEmptyName() {
        Director director = new Director(1L, " ");
        Set<ConstraintViolation<Director>> violationSet = validator.validate(director);
        assertEquals(1, violationSet.size());
        assertEquals("Name can't be null or empty", violationSet.iterator().next().getMessage());
    }

    @Test
    void createFilmNullName() {
        Director director = new Director(null, null);
        Set<ConstraintViolation<Director>> violationSet = validator.validate(director);
        assertEquals(1, violationSet.size());
        assertEquals("Name can't be null or empty", violationSet.iterator().next().getMessage());
    }
}
