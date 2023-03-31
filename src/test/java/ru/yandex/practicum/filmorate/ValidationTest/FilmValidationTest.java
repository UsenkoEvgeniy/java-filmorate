package ru.yandex.practicum.filmorate.ValidationTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilmValidationTest {
    final LocalDate correctDate = LocalDate.of(2000, 4, 4);
    final String name = "Correct name";
    final String description = "Correct description";
    final Integer duration = 30;
    static Validator validator;

    @BeforeAll
    public static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createFilmEmptyName() {
        Film film = new Film("", description, correctDate, duration);
        Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
        assertEquals(1, violationSet.size());
        assertEquals("Name can't be null or empty", violationSet.iterator().next().getMessage());
    }

    @Test
    void createFilmNullName() {
        Film film = new Film(null, description, correctDate, duration);
        Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
        assertEquals(1, violationSet.size());
        assertEquals("Name can't be null or empty", violationSet.iterator().next().getMessage());
    }

    @Test
    void createFilmWithDescriptionOver200() {
        Film film = new Film(name, "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. " +
                "Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient " +
                "montes, nascetur ridiculus mus. Donec qua", correctDate, duration);
        Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
        assertEquals(1, violationSet.size());
        assertEquals("Description must be no more than 200 symbols", violationSet.iterator().next().getMessage());
    }

    @Test
    void createFilmTooOld() {
        Film film = new Film(name, description, Film.START_DATE.minusDays(1), duration);
        Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
        assertEquals(1, violationSet.size());
        assertEquals("releaseDate is before 1895.12.28", violationSet.iterator().next().getMessage());
    }

    @Test
    void createFilmWith0Duration() {
        Film film = new Film(name, description, correctDate, 0);
        Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
        assertEquals(1, violationSet.size());
        assertEquals("The duration must be positive", violationSet.iterator().next().getMessage());
    }

    @Test
    void addFilmWithoutErrors() {
        Film film = new Film(name, description, correctDate, duration);
        Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
        assertEquals(0, violationSet.size());
    }
}