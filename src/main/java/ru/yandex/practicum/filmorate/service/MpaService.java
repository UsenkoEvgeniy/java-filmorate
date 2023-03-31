package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Service
@Slf4j
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Mpa getById(int id) {
        log.debug("Getting mpa rating with id: " + id);
        return mpaStorage.getById(id).orElseThrow(() -> new NotFoundException("Mpa not found with id=" + id));
    }

    public Collection<Mpa> getAll() {
        log.debug("Getting all films");
        return mpaStorage.getAll();
    }
}
