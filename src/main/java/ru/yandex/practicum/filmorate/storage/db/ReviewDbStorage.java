package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ReviewDbStorage {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Review> getReviews() {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id,\n" +
                "SUM(CASE\n" +
                "WHEN rl.is_like IS TRUE THEN 1 \n" +
                "WHEN rl.is_like IS FALSE THEN -1 " +
                "ELSE 0 END) AS useful\n" +
                "FROM review r  \n" +
                "LEFT JOIN REVIEW_LIKES rl on r.ID = rl.REVIEW_ID \n" +
                "GROUP BY r.ID\n" +
                "ORDER BY useful DESC ");
        List<Review> reviews = new ArrayList<>();
        while (reviewRows.next()) {
            Review review = convertSql(reviewRows);
            reviews.add(review);
        }
        return reviews;
    }

    public Review getReviewById(Integer id) {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id,\n" +
                "SUM(CASE\n" +
                "WHEN rl.is_like IS TRUE THEN 1 \n" +
                "WHEN rl.is_like IS FALSE THEN -1 " +
                "ELSE 0 END) AS useful\n" +
                "FROM review r  \n" +
                "LEFT JOIN REVIEW_LIKES rl on r.id = rl.review_id \n" +
                "where r.id = ? " +
                "GROUP BY r.id", id);
        if (reviewRows.next()) {
            return convertSql(reviewRows);
        } else {
            throw new NotFoundException("REVIEW NOT FOUND");
        }
    }

    public Review addReview(Review review) {
        String sqlQuery = "insert into review(content, is_positive, user_id, film_id) "
                + "values (?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, review.getContent());
            statement.setBoolean(2, review.getIsPositive());
            statement.setLong(3, review.getUserId());
            statement.setLong(4, review.getFilmId());
            return statement;
        }, kh);
        review.setReviewId(kh.getKey().longValue());
        return review;
    }

    public Review updateReview(Review review) {
        String sqlQuery = "UPDATE review " +
                "SET content = ?, is_positive = ? " +
                "WHERE id = ?";
        Review review1 = getReviewById(review.getReviewId().intValue());
        jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getReviewId());
        return getReviewById(review.getReviewId().intValue());
    }

    public Boolean deleteReview(Integer id) {
        String sqlDelete = "DELETE FROM review where id = ?";
        jdbcTemplate.update(sqlDelete, id);
        return true;
    }

    public List<Review> getMostUsefulReviews(Integer filmId, Integer count) {
        String sqlUseful = "SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id,\n" +
                "SUM(CASE\n" +
                "WHEN rl.IS_LIKE IS TRUE THEN 1 \n" +
                "WHEN rl.IS_LIKE IS FALSE THEN -1 " +
                "ELSE 0 END) AS useful\n" +
                "FROM review r  \n" +
                "LEFT JOIN REVIEW_LIKES rl on r.id = rl.review_id \n" +
                "WHERE r.film_id = ? " +
                "GROUP BY r.id\n" +
                "ORDER BY useful DESC ";
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet(sqlUseful, filmId);
        List<Review> reviewList = new ArrayList<>();
        while (reviewRows.next()) {
            Review review = convertSql(reviewRows);
            reviewList.add(review);
        }
        return reviewList.stream().limit(count).collect(Collectors.toList());
    }

    public Review likeReview(Integer id, Integer userId) {
        Review review = getReviewById(id);
        String sqlCheckReview = "SELECT * FROM review_likes WHERE review_id = ? and user_id = ?";
        SqlRowSet checkReview = jdbcTemplate.queryForRowSet(sqlCheckReview, id, userId);
        if (!checkReview.next()) {
            String sqlInsertLikes = "INSERT INTO review_likes (review_id, user_id, is_like) " + "values (?, ?, ?)";
            jdbcTemplate.update(sqlInsertLikes, id, userId, true);
        } else if (!checkReview.getBoolean("is_like")) {
            String sqlInsertLikes = "UPDATE review_likes SET is_like = ? WHERE review_id = ? and user_id = ?" + "values (?, ?, ?)";
            jdbcTemplate.update(sqlInsertLikes, true, id, userId);
        }
        return review;
    }

    public Review dislikeReview(Integer id, Integer userId) {
        Review review = getReviewById(id);
        String sqlCheckReview = "SELECT * FROM review_likes WHERE review_id = ? and user_id = ?";
        SqlRowSet checkReview = jdbcTemplate.queryForRowSet(sqlCheckReview, id, userId);
        if (!checkReview.next()) {
            String sqlInsertLikes = "INSERT INTO review_likes (review_id, user_id, is_like) " + "values (?, ?, ?)";
            jdbcTemplate.update(sqlInsertLikes, id, userId, false);
        } else if (checkReview.getBoolean("is_like")) {
            String sqlInsertLikes = "UPDATE review_likes SET is_like = ? WHERE review_id = ? and user_id = ?" + "values (?, ?, ?)";
            jdbcTemplate.update(sqlInsertLikes, false, id, userId);
        }
        return review;
    }

    public Review deleteLike(Integer id, Integer userId) {
        Review review = getReviewById(id);
        String sqlCheckReview = "SELECT * FROM review_likes WHERE review_id = ? and user_id = ?";
        SqlRowSet checkReview = jdbcTemplate.queryForRowSet(sqlCheckReview, id, userId);
        if (checkReview.next() && checkReview.getBoolean("is_like")) {
            String sqlInsertLikes = "DELETE FROM review_likes WHERE review_id = ? ";
            jdbcTemplate.update(sqlInsertLikes, id);
        }
        return review;
    }

    public Review deleteDislike(Integer id, Integer userId) {
        Review review = getReviewById(id);
        String sqlCheckReview = "SELECT * FROM review_likes WHERE review_id = ? and user_id = ?";
        SqlRowSet checkReview = jdbcTemplate.queryForRowSet(sqlCheckReview, id, userId);
        if (checkReview.next() && !checkReview.getBoolean("is_like")) {
            String sqlInsertLikes = "DELETE FROM review_likes WHERE review_id = ?";
            jdbcTemplate.update(sqlInsertLikes, id);
        }
        return review;
    }

    private Review convertSql(SqlRowSet rs) {
        return Review.builder()
                .reviewId(rs.getLong("id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }
}
