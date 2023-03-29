package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.storage.db.GenreDbStorage.genreMapper;

@Repository("FilmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Film> filmMapper = (rs, rowNum) -> {
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        Integer duration = rs.getInt("duration");
        Film film = new Film(name, description, releaseDate, duration);
        film.setId(rs.getLong("film_id"));
        film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
        return film;
    };

    public FilmDbStorage(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO film(name, description, release_date, duration, mpa_id) VALUES (:name, :desc, :release_date," +
                " :duration, :mpa)";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("desc", film.getDescription())
                .addValue("release_date", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("mpa", film.getMpa().getId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, sqlParameterSource, keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        log.debug("Film added with id: " + id);
        Set<Long> likes = film.getLikes();
        if (likes != null && !likes.isEmpty()) {
            log.debug("Update likes for film with id: " + id);
            sql = "INSERT INTO film_likes (film_id, user_id) VALUES (" + id + ", :user)";
            for (Long userId : likes) {
                jdbcTemplate.update(sql, Map.of("user", userId));
            }
        }
        Set<Genre> genres = film.getGenres();
        if (genres != null && !genres.isEmpty()) {
            log.debug("Update genres for film with id: " + id);
            sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (" + id + ", :genre)";
            for (Genre genre : genres) {
                jdbcTemplate.update(sql, Map.of("genre", genre.getId()));
            }
        }
        return getById(id);
    }

    @Override
    public Film updateFilm(Film film) {
        long id = film.getId();
        try {
            jdbcTemplate.queryForObject("SELECT film_id FROM film WHERE film_id=:id", Map.of("id", id), Long.class);
        } catch (IncorrectResultSizeDataAccessException e) {
            log.warn("There is no film in the database with id: " + id);
            throw new UserNotFoundException("Wrong id");
        }
        String sql = "UPDATE film SET film_id=:id, name=:name, description=:desc, release_date=:release_date, " +
                "duration=:duration, mpa_id=:mpa WHERE film_id=:id";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("desc", film.getDescription())
                .addValue("name", film.getName())
                .addValue("release_date", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("mpa", film.getMpa().getId());
        jdbcTemplate.update(sql, sqlParameterSource);
        log.debug("Update film with id: " + id);
        Set<Long> likes = film.getLikes();
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = :id", Map.of("id", id));
        if (likes != null && !likes.isEmpty()) {
            sql = "INSERT INTO film_likes (film_id, user_id) VALUES (" + id + ", :user)";
            log.debug("Updating likes for film with id: " + id);
            for (Long likeUserId : likes) {
                jdbcTemplate.update(sql, Map.of("user", likeUserId));
            }
        }
        Set<Genre> genres = film.getGenres();
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = :id", Map.of("id", id));
        if (genres != null && !genres.isEmpty()) {
            sql = "INSERT INTO film_genre (film_id, genre_id) VALUES(" + id + ", :genre)";
            log.debug("Updating genres for film with id: " + id);
            for (Genre genre : genres) {
                jdbcTemplate.update(sql, Map.of("genre", genre.getId()));
            }
        }
        return getById(film.getId());
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT f.name, description, release_date, duration, film_id, m.mpa_id, m.mpa_name " +
                "FROM film AS f LEFT JOIN mpa AS m ON f.mpa_id=m.mpa_id";
        log.debug("Getting all films");
        return jdbcTemplate.query(sql, filmMapper).stream().peek(f -> {
            f.setGenres(getGenresForFilmId(f.getId()));
            f.setLikes(getLikesSetForFilmId(f.getId()));
        }).collect(Collectors.toSet());
    }

    @Override
    public boolean deleteFilm(Film film) {
        String sqlQuery = "DELETE FROM film WHERE film_id = :id";
        log.debug("Delete film with id: " + film.getId());
        return jdbcTemplate.update(sqlQuery, Map.of("id", film.getId())) > 0;
    }

    @Override
    public Film getById(long id) {
        String sql = "SELECT f.name, description, release_date, duration, film_id, m.mpa_id, m.mpa_name " +
                "FROM film AS f LEFT JOIN mpa AS m ON f.mpa_id=m.mpa_id WHERE film_id = :id";
        try {
            log.debug("Getting film by id: " + id);
            Film film = jdbcTemplate.queryForObject(sql, Map.of("id", id), filmMapper);
            film.setGenres(getGenresForFilmId(id));
            film.setLikes(getLikesSetForFilmId(id));
            return film;
        } catch (IncorrectResultSizeDataAccessException e) {
            log.warn("Film not found for id: " + id);
            return null;
        }
    }

    private Set<Genre> getGenresForFilmId(long id) {
        log.debug("Getting genres for film: " + id);
        String sql = "SELECT film_id, g.genre_id, g.name FROM film_genre AS fg LEFT JOIN genre AS g ON fg.genre_id=" +
                "g.genre_ID WHERE film_id=:id";
        Set<Genre> resultSet = new TreeSet<>(Comparator.comparingInt(Genre::getId));
        resultSet.addAll(jdbcTemplate.query(sql, Map.of("id", id), genreMapper));
        return resultSet;
    }

    private Set<Long> getLikesSetForFilmId(long id) {
        log.debug("Getting likes for film: " + id);
        String sql = "SELECT user_id FROM film_likes WHERE film_id=:id";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Map.of("id", id), Long.class));
    }
}