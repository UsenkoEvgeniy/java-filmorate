package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class EventService {

    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    public EventService(@Qualifier("UserDbStorage") UserStorage userStorage, EventStorage eventStorage) {
        this.userStorage = userStorage;
        this.eventStorage = eventStorage;
    }

    public void addEvent(Event event) {
        log.debug("Adding event " + event);
        eventStorage.addEvent(event);
    }

    public Collection<Event> getUserEvents(long id) {
        User user = userStorage.getById(id);
        if (user == null) {
            log.warn("User with id {} doesn't exist", id);
            throw new UserNotFoundException(Long.toString(id));
        }
        return eventStorage.getUserEvents(user.getId());
    }

}
