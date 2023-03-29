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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository("UserDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserDbStorage(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users(email, login, name, birthday) VALUES (:email, :login, :name, :birthday)";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, sqlParameterSource, keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        log.debug("User created with id: " + id);
        Map<Long, String> friends = user.getFriends();
        if (friends.size() > 0) {
            sql = "INSERT INTO user_friends (user_id, friend_id, status) VALUES (" + user.getId() + ", :friend_id, :status)";
            log.debug("Set friends for user: " + id);
            for (Map.Entry<Long, String> entry : friends.entrySet()) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("friend_id", entry.getKey());
                paramMap.put("status", entry.getValue());
                jdbcTemplate.update(sql, paramMap);
            }
        }
        return getById(id);
    }

    @Override
    public User updateUser(User user) {
        Long id = user.getId();
        try {
            jdbcTemplate.queryForObject("SELECT user_id FROM users WHERE user_id=:id", Map.of("id", id), Long.class);
            log.debug("Updating user with id: " + id);
        } catch (IncorrectResultSizeDataAccessException e) {
            log.warn("There is no user in the database with id: " + id);
            throw new UserNotFoundException("Wrong id");
        }
        String sql = "UPDATE users SET email=:email, login=:login, name=:name, birthday=:birthday WHERE user_id=:id";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday());
        jdbcTemplate.update(sql, sqlParameterSource);
        Map<Long, String> friends = user.getFriends();
        jdbcTemplate.update("DELETE FROM user_friends WHERE user_id = :id", Map.of("id", user.getId()));
        if (friends.size() > 0) {
            log.debug("Updating friends for user with id: " + id);
            sql = "INSERT INTO user_friends (user_id, friend_id, status) VALUES (" + user.getId() + ", :friend_id, :status)";
            for (Map.Entry<Long, String> entry : friends.entrySet()) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("friend_id", entry.getKey());
                paramMap.put("status", entry.getValue());
                jdbcTemplate.update(sql, paramMap);
            }
        }
        return getById(user.getId());
    }

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        log.debug("Getting all users");
        return jdbcTemplate.query(sql, new UserMapper());
    }

    @Override
    public User getById(long id) {
        String sql = "SELECT * FROM users WHERE user_id = :id";
        try {
            return jdbcTemplate.queryForObject(sql, Map.of("id", id), new UserMapper());
        } catch (IncorrectResultSizeDataAccessException e) {
            log.warn("Nof found user for id: " + id);
            return null;
        }
    }

    @Override
    public boolean deleteUser(User user) {
        String sqlQuery = "DELETE FROM users WHERE user_id = :id";
        log.debug("Delete user with id: " + user.getId());
        return jdbcTemplate.update(sqlQuery, Map.of("id", user.getId())) > 0;
    }

    private final class UserMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            String email = rs.getString("email");
            String login = rs.getString("login");
            LocalDate birthday = rs.getDate("birthday").toLocalDate();
            User user = new User(email, login, birthday);
            user.setId(rs.getLong("user_id"));
            user.setName(rs.getString("name"));
            user.setFriends(getFriendsForUserId(user.getId()));
            return user;
        }

        private Map<Long, String> getFriendsForUserId(long id) {
            String sql = "SELECT friend_id, status FROM user_friends WHERE user_id = :id";
            List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, Map.of("id", id));
            return mapList.stream().collect(Collectors.toMap(map -> (Long) map.get("friend_id"), map -> (String) map.get("status")));
        }
    }
}