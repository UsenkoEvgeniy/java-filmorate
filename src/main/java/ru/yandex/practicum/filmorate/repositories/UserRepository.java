package ru.yandex.practicum.filmorate.repositories;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserRepository {
    User addUser(User user);

    User updateUser(User user);

    Collection<User> getAllUsers();
}
