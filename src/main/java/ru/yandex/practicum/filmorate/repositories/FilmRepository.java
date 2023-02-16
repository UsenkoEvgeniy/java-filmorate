package ru.yandex.practicum.filmorate.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@Repository
@Slf4j
public class FilmRepository {
    private final HashMap<Integer, Film> database = new HashMap<>();
    private Integer id = 1;

    public Film addFilm(Film film) {
        validate(film);
        film.setId(id++);
        database.put(film.getId(), film);
        log.info("Film added: " + film);
        return film;
    }

    public Film updateFilm(Film film) {
        validate(film);
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

    private boolean validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Film name is empty");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Description length is over 200 symbols");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Film release date must be after 1895.12.28");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Duration must be greater than 0");
        }
        return true;
    }
}
