package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    Collection<User> getAllUsers();

    User getById(long id);

    boolean deleteUser(User user);

    Collection<User> getUsersWithCommonTastes(long id);
}
