package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
@Slf4j
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<Director> getAll() {
        log.debug("Getting all directors");
        return directorStorage.getAll();
    }

    public Director getById(Long id) {
        log.debug("Get director with id {}", id);
        return directorStorage.getById(id).orElseThrow(() -> new NotFoundException("Director with id: " + id + " is not found"));
    }

    public Director create(Director director) {
        log.debug("Adding director " + director);
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        log.debug("Update director " + director);
        return directorStorage.update(director);
    }

    public void delete(Long id) {
        log.debug("Delete director with id {}", id);
        directorStorage.delete(id);
    }
}
