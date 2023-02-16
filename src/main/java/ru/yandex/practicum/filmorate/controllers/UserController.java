package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repositories.UserRepository;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository database;

    @PostMapping
    public User addUser(@RequestBody User user) {
        database.addUser(user);
        log.info("Post request for user");
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        database.updateUser(user);
        log.info("Put request for user");
        return user;
    }

   @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Get request for users");
        return database.getAllUsers();
    }
}
