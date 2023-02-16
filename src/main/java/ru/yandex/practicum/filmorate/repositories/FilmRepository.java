package ru.yandex.practicum.filmorate.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;

@Repository
@Slf4j
public class FilmRepository {
    private final HashMap<Integer, Film> database = new HashMap<>();
    private Integer id = 1;

    public Film addFilm(Film film) {
        film.setId(id++);
        database.put(film.getId(), film);
        log.info("Film added: " + film);
        return film;
    }

    public Film updateFilm(Film film) {
        if (!database.containsKey(film.getId())) {
            log.warn("Film id is not in db. Id: " + film.getId());
            throw new ValidationException("Incorrect film id for update");
        } else {
            database.put(film.getId(), film);
            log.info("Film updated: " + film);
            return film;
        }
    }

    public Collection<Film> getAllFilms() {
        return database.values();
    }
}
