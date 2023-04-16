package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {
    final GenreStorage genreStorage;

    @Test
    void testGetById() {
        Optional<Genre> genreOptional = genreStorage.getById(1);
        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия"));
    }

    @Test
    void getWithWrongId() {
        Optional<Genre> genre = genreStorage.getById(99);
        assertTrue(genre.isEmpty());
    }

    @Test
    void testGetAll() {
        Collection<Genre> genres = genreStorage.getAll();
        assertEquals(6, genres.size());
    }
}