package ru.yandex.practicum.filmorate.ValidationTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserValidationTest {
    final String email = "correct@email.com";
    final String login = "CorrectLogin";
    final LocalDate birthDay = LocalDate.of(2000, 1, 1);
    static Validator validator;

    @BeforeAll
    public static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void addUserEmptyEmail() {
        User user = new User("", login, birthDay);
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(1, violationSet.size());
        assertEquals("Email can't be null or empty", violationSet.iterator().next().getMessage());
    }

    @Test
    void addUserIncorrectEmail() {
        User user = new User("my.email.ru", login, birthDay);
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(1, violationSet.size());
        assertEquals("Incorrect email", violationSet.iterator().next().getMessage());
    }

    @Test
    void addUserEmptyLogin() {
        User user = new User(email, "", birthDay);
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(1, violationSet.size());
        assertEquals("No whitespaces allowed", violationSet.iterator().next().getMessage());
    }

    @Test
    void addUserLoginWithSpaces() {
        User user = new User(email, "lo gin", birthDay);
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(1, violationSet.size());
        assertEquals("No whitespaces allowed", violationSet.iterator().next().getMessage());
    }

    @Test
    void addUserEmptyName() {
        User user = new User(email, login, birthDay);
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(0, violationSet.size());
        assertEquals(user.getName(), user.getLogin(), "Name is not equal login");
    }

    @Test
    void addUserBirthdayInFuture() {
        User user = new User(email, login, LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(1, violationSet.size());
        assertEquals("Birthday must be in past", violationSet.iterator().next().getMessage());
    }

    @Test
    void addUserWithoutErrors() {
        User user = new User(email, login, birthDay);
        user.setName("name");
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(0, violationSet.size());
    }
}