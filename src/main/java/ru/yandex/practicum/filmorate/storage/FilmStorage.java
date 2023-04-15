package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getAllFilms();

    Collection<Film> getTopFilms(int size, int genreId, int year);

    boolean deleteFilm(Film film);

    Film getById(long id);

    Collection<Film> getFilmsForDirectorSorted(Long id, String sortBy);

    Collection<Film> getSearchResult(String query, String by);

    Collection<Film> getCommonFilms(long uid, long fid);

    List<Film> getSomeById(List<Long> ids);
}