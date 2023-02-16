package ru.yandex.practicum.filmorate.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

@SpringBootTest
class FilmRepositoryTest {

    @Autowired
    FilmRepository filmRepository;

    @Test
    void createFilmEmptyName() {
        Film film = new Film("", "description", LocalDate.of(2000, 4, 4), 30);
        assertThrows(ValidationException.class, () -> filmRepository.addFilm(film), "Added film empty name");
    }

    @Test
    void createFilmNullName() {
        Film film = new Film(null, "description", LocalDate.of(2000, 4, 4), 30);
        assertThrows(ValidationException.class, () -> filmRepository.addFilm(film), "Added film with null name");
    }

    @Test
    void createFilmWithDescriptionOver200() {
        Film film = new Film("film", "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. " +
                "Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient " +
                "montes, nascetur ridiculus mus. Donec qua", LocalDate.of(2000, 4, 4), 30);
        assertThrows(ValidationException.class, () -> filmRepository.addFilm(film), "Added film with description over 200 symbols");
    }

    @Test
    void createFilmTooOld() {
        Film film = new Film("film", "description", LocalDate.of(1895, 12, 27), 30);
        assertThrows(ValidationException.class, () -> filmRepository.addFilm(film), "Added film before 28/12/1895");
    }

    @Test
    void createFilmWith0Duration() {
        Film film = new Film("film", "description", LocalDate.of(2000, 4, 4), 0);
        assertThrows(ValidationException.class, () -> filmRepository.addFilm(film), "Added film with 0 duration");
    }

}
