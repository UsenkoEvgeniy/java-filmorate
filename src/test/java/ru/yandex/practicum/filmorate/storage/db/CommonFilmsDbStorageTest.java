package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommonFilmsDbStorageTest {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public CommonFilmsDbStorageTest(
            @Qualifier("UserDbStorage")UserStorage userStorage,
            @Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    private void fillFilmUserDb() {
        Film film1 = new Film("Film1 name", "First film description", LocalDate.now(), 100);
        film1.setMpa(new Mpa(1, ""));

        Film film2 = new Film("Film2 name", "Second film description", LocalDate.now(), 100);
        film2.setMpa(new Mpa(1, ""));

        Film film3 = new Film("Film3 title", "Third film description", LocalDate.now(), 100);
        film3.setMpa(new Mpa(1, ""));

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);
        filmStorage.addFilm(film3);

        User user1 = new User("first@user.ru", "user1", LocalDate.now().minusYears(10));
        user1.setName("user1 name");

        User user2 = new User("second@main.ru", "user2", LocalDate.now().minusYears(10));
        user2.setName("user2 name");

        userStorage.addUser(user1);
        userStorage.addUser(user2);
    }

    @Test
    @Order(1)
    void getCommontFilmsWithoutLikesTest() {
        fillFilmUserDb();
        Collection<Film> common = filmStorage.getCommonFilms(1, 2);
        assertEquals(0, common.size(), "У пользователей нет общих фильмов");
    }

    @Test
    @Order(2)
    void getCommonFilmsWithDifferentLikesTest() {
        Film film1 = filmStorage.getById(1L);
        film1.getLikes().add(1L);
        filmStorage.updateFilm(film1);

        Film film2 = filmStorage.getById(2L);
        film2.getLikes().add(2L);
        filmStorage.updateFilm(film2);

        Collection<Film> common = filmStorage.getCommonFilms(1, 2);
        assertEquals(0, common.size(), "У пользователей нет общих фильмов");
    }

    @Test
    @Order(3)
    void getCommonSingleFilmTest() {
        Film film = filmStorage.getById(3L);
        film.getLikes().add(1L);
        film.getLikes().add(2L);
        filmStorage.updateFilm(film);

        Collection<Film> common = filmStorage.getCommonFilms(1, 2);
        assertEquals(1, common.size(), "У пользователей должен быть один общий фильм");
        assertEquals(3L, common.iterator().next().getId(), "Фильм должен иметь id = 3");
    }

    @Test
    @Order(4)
    void getSeveralCommonFilmsTest() {
        Film film = filmStorage.getById(2L);
        film.getLikes().add(1L);
        filmStorage.updateFilm(film);
        Collection<Film> common = filmStorage.getCommonFilms(1, 2);
        assertEquals(2, common.size(), "У пользователей должен быть два общих фильма");
    }

    @Test
    @Order(5)
    void getCommonFilmsOneLikeRemoveedTest() {
        Film film = filmStorage.getById(3L);
        film.getLikes().remove(1L);
        filmStorage.updateFilm(film);
        Collection<Film> common = filmStorage.getCommonFilms(1, 2);
        assertEquals(1, common.size(), "У пользователей должен быть один общий фильм");
        assertEquals(2L, common.iterator().next().getId(), "Фильм должен иметь id = 2");
    }
}
