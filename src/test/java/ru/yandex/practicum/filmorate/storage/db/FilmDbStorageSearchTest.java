package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmDbStorageSearchTest {
    private EmbeddedDatabase embeddedDatabase;
    private NamedParameterJdbcTemplate jdbcTemplate;
    private FilmStorage filmStorage;
    private DirectorStorage directorStorage;
    private UserStorage userStorage;


    @BeforeEach
    void initDb() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addScript("schema.sql")
                .addScript("data.sql")
                .setType(EmbeddedDatabaseType.H2)
                .setName("testDb")
                .build();
        jdbcTemplate = new NamedParameterJdbcTemplate(embeddedDatabase);
        directorStorage = new DirectorDbStorage(jdbcTemplate);
        filmStorage = new FilmDbStorage(jdbcTemplate);
        userStorage = new UserDbStorage(jdbcTemplate);
        fillFilmDb();
    }

    @AfterEach
    void releaseDb() {
        embeddedDatabase.shutdown();
    }

    private void fillFilmDb() {
        Director director1 = directorStorage.create(new Director(1L, "First Director"));
        Director director2 = directorStorage.create(new Director(2L, "Second Director"));
        Director director3 = directorStorage.create(new Director(3L, "Third Director"));

        Film film1 = new Film("Film1 name", "First film description", LocalDate.now(), 100);
        film1.setDirectors(Set.of(director1));
        film1.setMpa(new Mpa(1, ""));
        Film film2 = new Film("Film2 name", "Second film description", LocalDate.now(), 100);
        film2.setDirectors(Set.of(director2));
        film2.setMpa(new Mpa(1, ""));
        Film film3 = new Film("Film3 title", "Third film description", LocalDate.now(), 100);
        film3.setDirectors(Set.of(director3));
        film3.setMpa(new Mpa(1, ""));

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);
        filmStorage.addFilm(film3);
    }

    private void fillUserDb() {
        User user1 = new User("first@user.ru", "user1", LocalDate.now().minusYears(10));
        user1.setName("user1 name");
        User user2 = new User("second@main.ru", "user2", LocalDate.now().minusYears(10));
        user2.setName("user2 name");
        userStorage.addUser(user1);
        userStorage.addUser(user2);
    }

    @Test
    void emptyQueryStringTestEmptyOrIncorrectByTest() {
        assertThrows(ValidationException.class, () -> filmStorage.getSearchResult("", ""),
                "Не заданы корректные поля для поиска");
        assertThrows(ValidationException.class, () -> filmStorage.getSearchResult("", "abyrvalg"),
                "Не заданы корректные поля для поиска");
    }

    @Test
    void emptyQueryStringCorrectByTest() {
        Collection<Film> searchResult = filmStorage.getSearchResult("", "title");
            assertEquals(3, searchResult.size(), "Поиск с пустой строкой должен вернуть все фильмы");
    }

    @Test
    void filmNotFoundTest() {
        Collection<Film> searchResult = filmStorage.getSearchResult("abyrvalg", "title,director");
        assertEquals(0, searchResult.size(), "Нет такого фильма");
    }

    @Test
    void filmWithDirectorFoundTest() {
        Collection<Film> searchResult = filmStorage.getSearchResult("First", "title,director");
        assertEquals(1, searchResult.size(), "Должен найти только один фильм с 'First Director'");
        assertTrue(searchResult.stream()
                        .findFirst()
                        .get()
                        .getDirectors()
                        .contains(new Director(1L, "First Director")),
                "Должен найти фильм с 'First Director'");
    }

    @Test
    void filmWithTitleFoundTest() {
        Collection<Film> searchResult = filmStorage.getSearchResult("ilm2 na", "title,director");
        assertEquals(1, searchResult.size(), "Должен найти только один фильм с 'ilm2 na'");
        assertTrue(searchResult.stream()
                        .findFirst()
                        .get()
                        .getName().equals("Film2 name"),
                "Должен найти фильм с 'Film2 name'");
    }

    @Test
    void filmWithTitleAndDirectorFoundCaseInsensitiveTest() {
        directorStorage.update(new Director(2L, "Third son"));
        Collection<Film> searchResult = filmStorage.getSearchResult("tHirD", "title,director");
        assertEquals(2, searchResult.size(), "Должен найти 2 фильма, id: 2,3");
    }

    @Test
    void filmSortingByRateSingleLikeTest() {
        fillUserDb();
        directorStorage.update(new Director(2L, "Third son"));
        Film film = filmStorage.getById(3L);
        film.getLikes().add(1L);
        filmStorage.updateFilm(film);
        Collection<Film> searchResult = filmStorage.getSearchResult("tHirD", "title,director");
        assertEquals(3, searchResult.iterator().next().getId(), "Первым должен быть фильм с id = 3");
        assertEquals(1, searchResult.iterator().next().getRate(), "Первым должен быть фильм с rate = 1");
    }

    @Test
    void filmSortingByRateMultipleLikesTest() {
        fillUserDb();
        directorStorage.update(new Director(2L, "Third son"));
        Film film = filmStorage.getById(3L);
        film.getLikes().add(1L);
        filmStorage.updateFilm(film);
        film = filmStorage.getById(2L);
        film.getLikes().add(1L);
        film.getLikes().add(2L);
        filmStorage.updateFilm(film);
        Collection<Film> searchResult = filmStorage.getSearchResult("tHirD", "title,director");
        assertEquals(2, searchResult.iterator().next().getId(), "Первым должен быть фильм с id = 2");
        assertEquals(2, searchResult.iterator().next().getRate(), "Первым должен быть фильм с rate = 2");
    }
}
