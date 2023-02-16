package ru.yandex.practicum.filmorate.repositories;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class UserRepositoryTest {
    static Validator validator;
    @BeforeAll
    public static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void addUserEmptyEmail() {
        User user = new User("", "login", LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(1, violationSet.size());
        assertEquals("Email can't be null or empty", violationSet.iterator().next().getMessage());
    }

    @Test
    void addUserIncorrectEmail() {
        User user = new User("my.email.ru", "login", LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(1, violationSet.size());
        assertEquals("Incorrect email", violationSet.iterator().next().getMessage());
    }

    @Test
    void addUserEmptyLogin() {
        User user = new User("my@email.ru", "", LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(1, violationSet.size());
        assertEquals("No whitespaces allowed", violationSet.iterator().next().getMessage());
    }

    @Test
    void addUserLoginWithSpaces() {
        User user = new User("my@email.ru", "lo gin", LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(1, violationSet.size());
        assertEquals("No whitespaces allowed", violationSet.iterator().next().getMessage());
    }

    @Test
    void addUserEmptyName() {
        User user = new User("my@email.ru", "login", LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(0, violationSet.size());
        assertEquals(user.getName(), user.getLogin(), "Name is not equal login");
    }

    @Test
    void addUserBirthdayInFuture() {
        User user = new User("my@email.ru", "login", LocalDate.of(2040, 1, 1));
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(1, violationSet.size());
        assertEquals("Birthday must be in past", violationSet.iterator().next().getMessage());
    }

    @Test
    void addUserWithoutErrors() {
        User user = new User("my@email.ru", "login", LocalDate.of(2000, 1, 1));
        user.setName("name");
        Set<ConstraintViolation<User>> violationSet = validator.validate(user);
        assertEquals(0, violationSet.size());
    }
}
