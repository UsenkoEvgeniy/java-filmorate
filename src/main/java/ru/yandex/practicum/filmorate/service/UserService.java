package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FilmService filmService;

    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, @Lazy FilmService filmService) {
        this.userStorage = userStorage;
        this.filmService = filmService;
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
        if (userStorage.getById(id) == null) {
            throw new UserNotFoundException("User with id " + id + " if not found");
        }
        Map<Long, List<Long>> usersWithCommonTastes = userStorage.getUsersWithCommonTastes(id);
        if (usersWithCommonTastes == null || !usersWithCommonTastes.containsKey(id) || usersWithCommonTastes.get(id) == null) {
            return Collections.emptyList();
        }
        List<Long> targetLikes = usersWithCommonTastes.get(id);
        usersWithCommonTastes.remove(id);
        List<Long> uniqueFilms = null;
        int maxSize = 0;
        for (Long u : usersWithCommonTastes.keySet()) {
            List<Long> intersectionList = new ArrayList<>(usersWithCommonTastes.get(u));
            List<Long> uniqueList = new ArrayList<>(usersWithCommonTastes.get(u));
            intersectionList.retainAll(targetLikes);
            uniqueList.removeAll(targetLikes);
            if (intersectionList.size() > maxSize && uniqueList.size() != 0) {
                maxSize = intersectionList.size();
                uniqueFilms = uniqueList;
            }
        }
        if (uniqueFilms == null) {
            return Collections.emptyList();
        }
        return filmService.getSomeById(uniqueFilms);
    }
}
