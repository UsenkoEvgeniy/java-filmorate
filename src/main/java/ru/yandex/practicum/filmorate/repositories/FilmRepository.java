package ru.yandex.practicum.filmorate.repositories;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

@Repository
public interface FilmRepository {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getAllFilms();
}
