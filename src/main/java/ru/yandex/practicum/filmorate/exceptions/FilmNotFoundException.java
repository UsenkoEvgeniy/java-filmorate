package ru.yandex.practicum.filmorate.exceptions;

public class FilmNotFoundException extends RuntimeException{
    public FilmNotFoundException() {
        super();
    }

    public FilmNotFoundException(String message) {
        super("FilmID: " + message);
    }
}