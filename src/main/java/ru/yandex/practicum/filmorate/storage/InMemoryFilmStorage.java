package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;

@Repository
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Integer, Film> database = new HashMap<>();
    private Integer id = 1;

    @Override
    public Film addFilm(Film film) {
        Integer filmId = id++;
        film.setId(filmId);
        database.put(filmId, film);
        log.info("Film added: " + film);
        return database.get(filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        Integer filmId = film.getId();
        if (!database.containsKey(filmId)) {
            log.warn("Film id is not in db. Id: " + filmId);
            throw new ValidationException("Incorrect film id for update");
        } else {
            database.put(filmId, film);
            log.info("Film updated: " + film);
            return database.get(filmId);
        }
    }

    @Override
    public Collection<Film> getAllFilms() {
        return database.values();
    }

    @Override
    public boolean deleteFilm(Film film) {
        return database.remove(film.getId()) != null;
    }

    @Override
    public Film getById(Integer id) {
        return database.get(id);
    }
}