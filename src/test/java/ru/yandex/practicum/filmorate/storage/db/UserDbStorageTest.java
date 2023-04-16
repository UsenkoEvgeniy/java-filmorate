package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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
        assertEquals(userFromDb.getName(), "name1");

        User user2 = new User("b@second.user", "login2", LocalDate.of(2020, 2, 2));
        user2.setName("name2");
        User userFromDb2 = userStorage.addUser(user2);
        assertEquals(userFromDb2.getName(), "name2");
    }

    @Test
    void updateUser() {
        User user = new User("new@mail.ru", "l1234", LocalDate.of(2020, 1, 1));
        user.setName("n1234");
        User userFromDb = userStorage.addUser(user);

        User userUp = new User("u@updated.user", "loginU", LocalDate.of(2020, 1, 1));
        userUp.setName("nameU");
        userUp.setId(userFromDb.getId());
        User userFromDbUpdate = userStorage.updateUser(userUp);
        assertEquals("nameU", userFromDbUpdate.getName());
    }

    @Test
    void getAllUsers() {
        User user = new User("a@first.user", "login12", LocalDate.of(2020, 1, 1));
        user.setName("name1");
        User userFromDb = userStorage.addUser(user);
        assertTrue(userStorage.getAllUsers().stream().collect(Collectors.toMap(User::getId, x -> x)).containsKey(userFromDb.getId()));
    }

    @Test
    void getById() {
        User user = new User("id@.ru", "lid", LocalDate.of(2020, 1, 1));
        user.setName("nid");
        User userFromDb = userStorage.addUser(user);
        assertEquals("nid", userStorage.getById(userFromDb.getId()).getName());
    }

    @Test
    void getWithWrongId() {
        assertNull(userStorage.getById(-3));
    }

    @Test
    void deleteUser() {
        User user = new User("delete@mail.ru", "l1234", LocalDate.of(2020, 1, 1));
        user.setName("name1");
        User userFromDb = userStorage.addUser(user);
        assertTrue(userStorage.deleteUser(userFromDb));
        assertFalse(userStorage.getAllUsers().stream().collect(Collectors.toMap(User::getId, x -> x))
                .containsKey(userFromDb.getId()));
    }
}