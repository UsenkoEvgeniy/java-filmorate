package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
@Component
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Event event) {
        String sql = "INSERT INTO event (user_id, entity_id, time_stamp, event_type, operation) " +
                "VALUES (?, ?, ?, ?, ?);";
        jdbcTemplate.update(sql, event.getUserId(), event.getEntityId(), event.getTimestamp(), event.getEventType(), event.getOperation());
        long eventId = jdbcTemplate.queryForObject("SELECT event_id FROM event ORDER BY event_id DESC LIMIT 1;", Long.class);
        event.setEventId(eventId);
    }

    @Override
    public Event getEventById(long id) {
        String sql = "SELECT * FROM event WHERE event_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeEvent(rs), id).get(0);
    }

    @Override
    public Collection<Event> getUserEvents(long id) {
        String sql = "SELECT * FROM event WHERE user_id = ?;";
        return new ArrayList<>(jdbcTemplate.query(sql, (rs, rowNum) -> makeEvent(rs), id));
    }

    private Event makeEvent(ResultSet rs) throws SQLException {
        return Event.builder()
                .eventId(rs.getInt("event_id"))
                .userId(rs.getInt("user_id"))
                .entityId(rs.getInt("entity_id"))
                .timestamp(rs.getLong("time_stamp"))
                .eventType(rs.getString("event_type"))
                .operation(rs.getString("operation"))
                .build();
    }
}
