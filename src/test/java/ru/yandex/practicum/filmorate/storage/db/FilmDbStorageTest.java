package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    final GenreStorage genreStorage;
    final FilmService filmService;
    final FilmStorage filmStorage;
    final UserStorage userStorage;

    @Test
    void addFilm() {
        Film film = new Film("First Movie", "First desc", LocalDate.of(2020, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        Film filmWithId = filmStorage.addFilm(film);
        assertEquals(1, filmWithId.getId());
        assertEquals("G", filmWithId.getMpa().getName());

        Film film2 = new Film("Second Movie", "Second desc", LocalDate.of(2020, 3, 2), 220);
        film2.setMpa(new Mpa(2, null));
        Film filmWithId2 = filmStorage.addFilm(film2);
        assertEquals(2, filmWithId2.getId());
        assertEquals("PG", filmWithId2.getMpa().getName());
    }

    @Test
    void updateFilm() {

        Film film = new Film("Updated Movie", "First desc", LocalDate.of(2020, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        film.setId(1);
        Film filmFromDb = filmStorage.updateFilm(film);
        assertEquals(1, filmFromDb.getId());
        assertEquals(film.getName(), filmFromDb.getName());
    }

    @Test
    void getAllFilms() {
        Collection<Film> films = filmStorage.getAllFilms();
        assertEquals(2, films.size());
    }

    @Test
    void deleteFilm() {
        assertTrue(filmStorage.deleteFilm(filmStorage.getById(1)));
    }

    @Test
    void getById() {
        assertEquals("Second Movie", filmStorage.getById(2).getName());
    }

    @Test
    void testGetByWithWrongId() {
        assertNull(filmStorage.getById(-3));
    }

    @Test
    void getTopFilms() {
        User user = new User("a@first.user", "login1", LocalDate.of(2020, 1, 1));
        user.setName("name1");
        userStorage.addUser(user);
        User user2 = new User("b@second.user", "login2", LocalDate.of(2020, 2, 2));
        user2.setName("name2");
        userStorage.addUser(user2);
        long user1Id = userStorage.getAllUsers().stream().filter(x -> x.getName().equals("name1")).findFirst().get().getId();
        long user2Id = userStorage.getAllUsers().stream().filter(x -> x.getName().equals("name2")).findFirst().get().getId();

        Film film = new Film("First Movie", "First desc", LocalDate.of(2019, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        Film film2 = new Film("Second Movie", "Second desc", LocalDate.of(2020, 3, 2), 220);
        film2.setMpa(new Mpa(2, null));
        Set<Genre> genresFilm1 = new HashSet<>();
        genresFilm1.add(genreStorage.getById(1).get());
        genresFilm1.add(genreStorage.getById(2).get());
        Set<Genre> genresFilm2 = new HashSet<>();
        genresFilm2.add(genreStorage.getById(1).get());

        film.setGenres(genresFilm1);
        film2.setGenres(genresFilm2);
        filmStorage.addFilm(film);
        filmStorage.addFilm(film2);
        long film1Id = filmStorage.getAllFilms().stream().filter(x -> x.getName().equals("First Movie")).findFirst().get().getId();
        long film2Id = filmStorage.getAllFilms().stream().filter(x -> x.getName().equals("Second Movie")).findFirst().get().getId();

        filmService.addLike(user1Id, film2Id);
        filmService.addLike(user2Id, film2Id);
        filmService.addLike(user1Id, film1Id);

        assertEquals(filmService.getTopFilms(2, genreStorage.getById(2).get().getId(), 0).size(), 1, "sizes are diff");
        assertEquals(new ArrayList<>(filmService.getTopFilms(2, genreStorage.getById(2).get().getId(), 0))
                .get(0).getName(), "First Movie", "names are diff");
        assertEquals(new ArrayList<>(filmService.getTopFilms(2, 0, 2019))
                .get(0).getName(), "First Movie", "names are diff");
    }
}