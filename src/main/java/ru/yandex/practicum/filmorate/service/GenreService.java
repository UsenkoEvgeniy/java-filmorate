package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Service
@Slf4j
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Genre getById(int id) {
        log.debug("Getting genre with id: " + id);
        return genreStorage.getById(id).orElseThrow(() -> new NotFoundException("Genre not found with id=" + id));
    }

    public Collection<Genre> getAll() {
        log.debug("Getting all genres");
        return genreStorage.getAll();
    }
}
