package ru.yandex.practicum.filmorate.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void addUserEmptyEmail() {
        User user = new User("", "login", LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> userRepository.addUser(user), "Empty email");
    }

    @Test
    void addUserIncorrectEmail() {
        User user = new User("my.email.ru", "login", LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> userRepository.addUser(user), "Email without @");
    }

    @Test
    void addUserEmptyLogin() {
        User user = new User("my@email.ru", "", LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> userRepository.addUser(user), "Empty login");
    }

    @Test
    void addUserLoginWithSpaces() {
        User user = new User("my@email.ru", "lo gin", LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> userRepository.addUser(user), "Login with spaces");
    }

    @Test
    void addUserEmptyName() {
        User user = new User("my@email.ru", "login", LocalDate.of(2000, 1, 1));
        userRepository.addUser(user);
        assertEquals(userRepository.getAllUsers().iterator().next().getName(), user.getLogin(), "Name is not equal login");
    }

    @Test
    void addUserBirthdayInFuture() {
        User user = new User("my@email.ru", "lo gin", LocalDate.of(2040, 1, 1));
        assertThrows(ValidationException.class, () -> userRepository.addUser(user), "Birthday in future");
    }
}
