package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class FilmDbStorageTest {

    final FilmStorage filmStorage;

    public FilmDbStorageTest(@Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

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
    void testGetByWithWrongId(){
        assertNull(filmStorage.getById(-3));
    }
}