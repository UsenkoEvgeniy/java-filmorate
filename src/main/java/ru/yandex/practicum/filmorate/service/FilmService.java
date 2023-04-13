package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film addFilm(Film film) {
        log.debug("Adding film " + film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        log.debug("Updating film " + film);
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAllFilms() {
        log.debug("Getting all films");
        return filmStorage.getAllFilms();
    }

    public void addLike(long userId, long filmId) {
        User user = userService.getUserById(userId);
        Film film = getFilmById(filmId);
        log.debug("Adding like to film: {} from user: {}", film, user);
        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
    }

    public void removeLike(long userId, long filmId) {
        User user = userService.getUserById(userId);
        Film film = getFilmById(filmId);
        log.debug("Removing like to film: {} from user: {}", film, user);
        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
    }

    public Collection<Film> getTopFilms(Integer size) {
        log.debug("Get top {} films", size);
        return filmStorage.getTopFilms(size);
    }

    public Film getFilmById(long filmId) {
        Film film = filmStorage.getById(filmId);
        if (film == null) {
            log.warn("Film with id {} doesn't exist", filmId);
            throw new FilmNotFoundException(Long.toString(filmId));
        }
        log.debug("Get film with id: {}", filmId);
        return film;
    }

    public Collection<Film> getFilmsForDirector(Long id, String sortBy) {
        log.debug("Get films by director with id: {} sorted by {}", id, sortBy);
        return filmStorage.getFilmsForDirectorSorted(id, sortBy);
    }

    public void deleteFilm(long id) {
        if (!filmStorage.deleteFilm(filmStorage.getById(id))) {
            throw new FilmNotFoundException("Film with id " + id + " not found!");
        }
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        long uid = userService.getUserById(userId).getId();
        long fid = userService.getUserById(friendId).getId();
        return filmStorage.getCommonFilms(uid, fid);
    }
}