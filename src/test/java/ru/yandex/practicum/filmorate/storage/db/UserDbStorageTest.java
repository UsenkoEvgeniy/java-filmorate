package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserDbStorageTest {

    final UserStorage userStorage;

    public UserDbStorageTest(@Qualifier("UserDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Test
    void addUser() {
        User user = new User("a@first.user", "login1", LocalDate.of(2020, 1, 1));
        user.setName("name1");
        User userFromDb = userStorage.addUser(user);
        assertEquals(1, userFromDb.getId());

        User user2 = new User("b@second.user", "login2", LocalDate.of(2020, 2, 2));
        user2.setName("name2");
        User userFromDb2 = userStorage.addUser(user2);
        assertEquals(2, userFromDb2.getId());
    }

    @Test
    void updateUser() {
        User user = new User("u@updated.user", "loginU", LocalDate.of(2020, 1, 1));
        user.setName("nameU");
        user.setId(1);
        User userFromDb = userStorage.updateUser(user);
        assertEquals(1, userFromDb.getId());
        assertEquals("nameU", userFromDb.getName());
    }

    @Test
    void getAllUsers() {
        assertEquals(2, userStorage.getAllUsers().size());
    }

    @Test
    void getById() {
        assertEquals("name2", userStorage.getById(2).getName());
    }

    @Test
    void getWithWrongId() {
        assertNull(userStorage.getById(-3));
    }

    @Test
    void deleteUser() {
        User user = userStorage.getById(1);
        assertTrue(userStorage.deleteUser(user));
        assertEquals(1, userStorage.getAllUsers().size());
    }
}