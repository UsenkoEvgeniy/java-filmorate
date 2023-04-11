package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DirectorDbStorageTest {
    final DirectorStorage directorStorage;

    @BeforeEach
    void beforeEach() {
        Director director1 = new Director(null, "Director 1");
        Director director2 = new Director(null, "Director 2");
        directorStorage.create(director1);
        directorStorage.create(director2);
    }

    @Test
    void getById() {
        Director director = directorStorage.getById(1L).get();
        assertEquals(1, director.getId());
        assertEquals("Director 1", director.getName());
    }

    @Test
    void getWrongId() {
        long wrongId = 99;
        assertEquals(Optional.empty(), directorStorage.getById(wrongId));
    }

    @Test
    void create() {
        Director director = directorStorage.create(new Director(null, "Director"));
        assertEquals(3, director.getId());
        assertEquals("Director", director.getName());
    }

    @Test
    void update() {
        Director director = directorStorage.update(new Director(1L, "Updated"));
        assertEquals(1, director.getId());
        assertEquals("Updated", director.getName());
    }

    @Test
    void updateWrongId() {
        Long wrongId = 99L;
        Director directorWithWrongId = new Director(wrongId, "Wrong Id");
        assertThrows(NotFoundException.class, () -> directorStorage.update(directorWithWrongId));
    }

    @Test
    void delete() {
        directorStorage.delete(1L);
        assertEquals(Optional.empty(), directorStorage.getById(1L));
    }

    @Test
    void deleteWrongId() {
        Long wrongId = 99L;
        assertThrows(NotFoundException.class, () -> directorStorage.delete(wrongId));
    }
}