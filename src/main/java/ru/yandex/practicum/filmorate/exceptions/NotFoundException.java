package ru.yandex.practicum.filmorate.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super();
    }

    public NotFoundException(String message) {
        super("GenreID: " + message);
    }
}
