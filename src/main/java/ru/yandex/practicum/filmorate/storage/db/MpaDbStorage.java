package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Mpa> mpaMapper = (rs, rowNum) -> new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name"));

    public MpaDbStorage(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        jdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<Mpa> getById(int id) {
        String sql = "SELECT * FROM mpa WHERE mpa_id=:id";
        try {
            Mpa mpa = jdbcTemplate.queryForObject(sql, Map.of("id", id), mpaMapper);
            log.debug("Getting mpa rating with id: " + id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Empty sql response for mpa id: " + id);
            return Optional.empty();
        }
    }

    @Override
    public Collection<Mpa> getAll() {
        String sql = "SELECT * FROM mpa";
        log.debug("Getting all mpa ratings");
        return jdbcTemplate.query(sql, mpaMapper);
    }
}