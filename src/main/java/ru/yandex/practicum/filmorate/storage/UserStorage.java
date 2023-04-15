package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    Collection<User> getAllUsers();

    User getById(long id);

    boolean deleteUser(User user);

    Map<Long, List<Long>> getUsersWithCommonTastes(long id);
}
