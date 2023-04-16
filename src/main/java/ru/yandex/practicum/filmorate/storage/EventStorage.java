package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.event.Event;

import java.util.Collection;

public interface EventStorage {

    void addEvent(Event event);

    Event getEventById(long id);

    Collection<Event> getUserEvents(long id);
}
