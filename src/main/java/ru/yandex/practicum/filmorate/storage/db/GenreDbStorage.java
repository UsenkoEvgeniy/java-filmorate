package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class GenreDbStorage implements GenreStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    static final RowMapper<Genre> genreMapper = (rs, rowNum) -> new Genre(rs.getInt("genre_id"), rs.getString("name"));

    public GenreDbStorage(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        jdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<Genre> getById(int id) {
        String sql = "SELECT genre_id, name FROM genre WHERE genre_id=:id";
        try {
            Genre genre = jdbcTemplate.queryForObject(sql, Map.of("id", id), genreMapper);
            log.debug("Getting genre with id: " + id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Empty sql response for genre with id = " + id);
            return Optional.empty();
        }
    }

    @Override
    public Collection<Genre> getAll() {
        String sql = "SELECT genre_id, name FROM genre";
        log.debug("Getting all genres");
        return jdbcTemplate.query(sql, genreMapper);
    }
}