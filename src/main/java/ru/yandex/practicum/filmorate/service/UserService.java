package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, @Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public User addUser(User user) {
        log.debug("Adding user: " + user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        log.debug("Updating user: " + user);
        return userStorage.updateUser(user);
    }

    public Collection<User> getAllUsers() {
        log.debug("Getting all users");
        return userStorage.getAllUsers();
    }

    public void addFriend(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        log.debug("Adding friend: {} to user: {}", friend, user);
        user.getFriends().put(friendId, "Requested");
        userStorage.updateUser(user);
    }

    public void removeFriend(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        log.debug("Removing friend: {} from user: {}", friend, user);
        user.getFriends().remove(friendId);
        userStorage.updateUser(user);
    }

    public Collection<User> getCommonFriendsList(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        log.debug("Get common friend for friend: {} and user: {}", friend, user);
        Set<Long> commonFriends = new HashSet<>(user.getFriends().keySet());
        commonFriends.retainAll(friend.getFriends().keySet());
        return commonFriends.stream().map(userStorage::getById).collect(Collectors.toList());
    }

    public Collection<User> getFriendsList(long userId) {
        User user = getUserById(userId);
        log.debug("Get list of friends for user {}", user);
        return user.getFriends().keySet().stream().map(userStorage::getById).collect(Collectors.toList());
    }

    public User getUserById(long id) {
        User user = userStorage.getById(id);
        if (user == null) {
            log.warn("User with id {} doesn't exist", id);
            throw new UserNotFoundException(Long.toString(id));
        }
        log.debug("Get user with id: {}", id);
        return user;
    }

    public void deleteUser(long id) {
        if (!userStorage.deleteUser(userStorage.getById(id))) {
            throw new UserNotFoundException("User with id " + id + " if not found");
        }
    }

    public Collection<Film> getRecommendation(long id) {
        Collection<Long> targetUser = userStorage.getById(id).getLikes();
        Collection<User> usersWithCommonTastes = userStorage.getUsersWithCommonTastes(id);
        if (targetUser.isEmpty() || usersWithCommonTastes.isEmpty()) {
            return Collections.emptyList();
        }
        HashMap<User, List<Long>> usersWithIntersections = new HashMap<>();
        HashMap<User, List<Long>> usersWithUniques = new HashMap<>();
        List<Long> secondaryUserIntersections;
        List<Long> secondaryUserUniques;
        int maxSize = 0;
        User user = null;
        for(User u : usersWithCommonTastes) {
            for (Long filmId : u.getLikes()) {
                secondaryUserUniques = null;
                if (!usersWithIntersections.containsKey(u)) {
                    secondaryUserIntersections = List.of(filmId);
                    usersWithIntersections.put(u, secondaryUserIntersections);
                } else {
                    secondaryUserIntersections = new ArrayList<>(usersWithIntersections.get(u));
                    secondaryUserIntersections.add(filmId);
                    usersWithIntersections.put(u, secondaryUserIntersections);
                }
                if (!targetUser.contains(filmId)) {
                    if (!usersWithUniques.containsKey(u)) {
                        secondaryUserUniques = List.of(filmId);
                        usersWithUniques.put(u, secondaryUserUniques);
                    } else {
                        secondaryUserUniques = usersWithUniques.get(u);
                        secondaryUserUniques.add(filmId);
                        usersWithUniques.put(u, secondaryUserUniques);
                    }
                }
                if (secondaryUserIntersections.size() > maxSize && secondaryUserUniques != null) {
                    maxSize = secondaryUserIntersections.size();
                    user = u;
                }
            }
        }
        if (usersWithUniques.get(user) == null) {
            return Collections.emptyList();
        }
        User finalUser = user;
        return filmStorage.getAllFilms().stream().filter(f -> usersWithUniques.get(finalUser).contains(f.getId())).collect(Collectors.toList());
    }
}