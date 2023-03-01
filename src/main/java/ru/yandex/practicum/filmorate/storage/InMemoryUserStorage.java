package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;

@Repository
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> database = new HashMap<>();
    private Integer id = 1;

    @Override
    public User addUser(User user) {
        Integer userId = id++;
        user.setId(userId);
        log.info("User added: " + user);
        database.put(user.getId(), user);
        return database.get(userId);
    }

    @Override
    public User updateUser(User user) {
        Integer userId = user.getId();
        if (!database.containsKey(userId)) {
            log.warn("There is no user in the database with id: " + userId);
            throw new ValidationException("Wrong id");
        } else {
            log.info("Update user id: " + userId);
            database.put(user.getId(), user);
            return database.get(userId);
        }
    }

    @Override
    public Collection<User> getAllUsers() {
        log.info("Get request /users");
        return database.values();
    }

    @Override
    public User getById(Integer id) {
        return database.get(id);
    }

    @Override
    public boolean deleteUser(User user) {
        return database.remove(user.getId()) != null;
    }
}