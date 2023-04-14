package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
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
import java.util.*;

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
            sql = "INSERT INTO user_friends (user_id, friend_id, status) VALUES (:id, :friend_id, :status)";
            log.debug("Set friends for user: " + id);
            SqlParameterSource[] params = friends.entrySet().stream()
                    .map(friend -> new MapSqlParameterSource().addValue("friend_id", friend.getKey())
                            .addValue("status", friend.getValue())
                            .addValue("id", user.getId()))
                    .toArray(SqlParameterSource[]::new);
            jdbcTemplate.batchUpdate(sql, params);
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
            sql = "INSERT INTO user_friends (user_id, friend_id, status) VALUES (:id, :friend_id, :status)";
            SqlParameterSource[] params = friends.entrySet().stream()
                    .map(friend -> new MapSqlParameterSource().addValue("friend_id", friend.getKey())
                            .addValue("status", friend.getValue())
                            .addValue("id", user.getId()))
                    .toArray(SqlParameterSource[]::new);
            jdbcTemplate.batchUpdate(sql, params);
        }
        return getById(user.getId());
    }

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT u.user_id, email, login, name, birthday, friend_id, status, l.film_id " +
                "FROM users u " +
                "LEFT JOIN user_friends f ON u.user_id=f.user_id " +
                "LEFT JOIN film_likes l ON u.user_id=l.user_id";
        log.debug("Getting all users");
        return jdbcTemplate.query(sql, new UserWithFriendsMapper());
    }

    @Override
    public User getById(long id) {
        String sql = "SELECT u.user_id, email, login, name, birthday, friend_id, status, l.film_id " +
                "FROM users u " +
                "LEFT JOIN user_friends f ON u.user_id=f.user_id " +
                "LEFT JOIN film_likes l ON u.user_id=l.user_id " +
                "WHERE u.user_id = :id";
        List<User> userList = jdbcTemplate.query(sql, Map.of("id", id), new UserWithFriendsMapper());
        if (userList.isEmpty()) {
            log.warn("Nof found user for id: " + id);
            return null;
        }
        return userList.get(0);
    }

    @Override
    public boolean deleteUser(User user) {
        String sqlQuery = "DELETE FROM users WHERE user_id = :id";
        log.debug("Delete user with id: " + user.getId());
        return jdbcTemplate.update(sqlQuery, Map.of("id", user.getId())) > 0;
    }

    @Override
    public Collection<User> getUsersWithCommonTastes(long id) {
        String sql = "SELECT u.user_id, email, login, name, birthday, friend_id, status, l.film_id " +
                "FROM users u " +
                "LEFT JOIN user_friends f ON u.user_id=f.user_id " +
                "LEFT JOIN film_likes l ON u.user_id=l.user_id " +
                "LEFT JOIN (SELECT fl1.user_id, fl1.film_id FROM film_likes fl1 " +
                "LEFT JOIN (SELECT * FROM film_likes WHERE user_id =:id) as fl2 ON fl1.film_id = fl2.film_id) as su ON l.user_id = su.user_id " +
                "WHERE su.user_id <>:id";

        return jdbcTemplate.query(sql, Map.of("id", id), new UserWithFriendsMapper());
    }

    private static final class UserWithFriendsMapper implements ResultSetExtractor<List<User>> {
        @Override
        public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, User> map = new HashMap<>();
            User user;
            while (rs.next()) {
                Long userId = rs.getLong("user_id");
                user = map.computeIfAbsent(userId, id -> {
                    try {
                        String email = rs.getString("email");
                        String login = rs.getString("login");
                        LocalDate birthday = rs.getDate("birthday").toLocalDate();
                        User newUser = new User(email, login, birthday);
                        newUser.setId(rs.getLong("user_id"));
                        newUser.setName(rs.getString("name"));
                        return newUser;
                    } catch (SQLException e) {
                        log.warn("SQL Exception for user: " + id);
                        throw new RuntimeException("Bad sql request" + e);
                    }
                });
                Map<Long, String> friendMap = user.getFriends();
                Long friendId = rs.getLong("friend_id");
                if (friendId != 0) {
                    friendMap.put(friendId, rs.getString("status"));
                }
                Set<Long> filmSet = user.getLikes();
                Long filmId = rs.getLong("film_id");
                if (filmId != 0) {
                    filmSet.add(filmId);
                }
            }
            return new ArrayList<>(map.values());
        }
    }
}