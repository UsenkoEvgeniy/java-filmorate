package ru.yandex.practicum.filmorate.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException() {
        super();
    }

    public UserNotFoundException(String message) {
        super("UserID: " + message);
    }
}