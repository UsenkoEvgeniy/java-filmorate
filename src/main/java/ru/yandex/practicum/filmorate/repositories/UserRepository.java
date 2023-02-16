package ru.yandex.practicum.filmorate.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@Repository
@Slf4j
public class UserRepository {
    private final HashMap<Integer, User> database = new HashMap<>();
    private Integer id = 1;

    public User addUser(User user) {
        validate(user);
        user.setId(id++);
        log.info("User added: " + user);
        return database.put(user.getId(), user);
    }

    public User updateUser(User user) {
        validate(user);
        if (!database.containsKey(user.getId())) {
            log.warn("There is no user in the database with id: " + user.getId());
            throw new ValidationException("Wrong id");
        } else {
            log.info("Update user id: " + user.getId());
            return database.put(user.getId(), user);
        }
    }

    public Collection<User> getAllUsers() {
        log.info("Get request /users");
        return database.values();
    }

    private boolean validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Wrong email");
            throw new ValidationException("Email is not valid");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Wrong login");
            throw new ValidationException("Login is empty");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Name is empty, login used for name");
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Birthday is not valid");
            throw new ValidationException("Birthday is null or in the future");
        }
        return true;
    }
}
