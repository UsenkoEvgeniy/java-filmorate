package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Review> getReviews() {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("select * from review"
                + " order by useful desc");
        List<Review> reviewList = new ArrayList<>();
        while (reviewRows.next()) {
            Review review = Review.builder()
                    .reviewId(reviewRows.getInt("id"))
                    .content(reviewRows.getString("content"))
                    .isPositive(reviewRows.getBoolean("is_positive"))
                    .userId(reviewRows.getInt("user_id"))
                    .filmId(reviewRows.getInt("film_id"))
                    .useful(reviewRows.getInt("useful"))
                    .build();
            reviewList.add(review);
        }
        return reviewList;
    }

    public Review getReviewById(Integer id) {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("select * from review where id = ?", id);
        if (reviewRows.next()) {
            return Review.builder()
                    .reviewId(reviewRows.getInt("id"))
                    .content(reviewRows.getString("content"))
                    .isPositive(reviewRows.getBoolean("is_positive"))
                    .userId(reviewRows.getInt("user_id"))
                    .filmId(reviewRows.getInt("film_id"))
                    .useful(reviewRows.getInt("useful"))
                    .build();
        } else {
            throw new NotFoundException("REVIEW NOT FOUND");
        }
    }

    public Review addReview(Review review) {
        String sqlQuery = "insert into review(content, is_positive, user_id, film_id, useful) " + "values (?, ?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, review.getContent());
            statement.setBoolean(2, review.getIsPositive());
            statement.setInt(3, review.getUserId());
            statement.setInt(4, review.getFilmId());
            statement.setInt(5, review.getUseful());
            return statement;
        }, kh);
        review.setReviewId(kh.getKey().intValue());
        return review;
    }

    public Review updateReview(Review review) {
        String sqlQuery = "update REVIEW " + "set content = ?, is_positive = ? " + "where ID = ?";
        Review review1 = getReviewById(review.getReviewId());
        jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getReviewId());
        return getReviewById(review.getReviewId());
    }

    public Integer deleteReview(Integer id) {
        String sqlDelete = "delete from REVIEW where id = ?";
        jdbcTemplate.update(sqlDelete, id);
        return id;
    }

    public List<Review> getMostUsefulReviews(Integer filmId, Integer count) {
        String sqlUseful = "SELECT * " + "FROM REVIEW r " + " WHERE FILM_ID = ?" + "ORDER BY USEFUL DESC";
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet(sqlUseful, filmId);
        List<Review> reviewList = new ArrayList<>();
        while (reviewRows.next()) {
            Review review = Review.builder()
                    .reviewId(reviewRows.getInt("id"))
                    .content(reviewRows.getString("content"))
                    .isPositive(reviewRows.getBoolean("is_positive"))
                    .userId(reviewRows.getInt("user_id"))
                    .filmId(reviewRows.getInt("film_id"))
                    .useful(reviewRows.getInt("useful"))
                    .build();
            reviewList.add(review);
        }
        return reviewList.stream().limit(count).collect(Collectors.toList());
    }

    public Review likeReview(Integer id, Integer userId) {
        Review review = getReviewById(id);
        String sqlCheckReview = "select * from REVIEW_LIKES where review_id = ? and user_id = ?";
        SqlRowSet checkReview = jdbcTemplate.queryForRowSet(sqlCheckReview, id, userId);
        if (!checkReview.next()) {
            String sqlInsertLikes = "insert into REVIEW_LIKES (review_id, user_id, is_like) " + "values (?, ?, ?)";
            jdbcTemplate.update(sqlInsertLikes, id, userId, true);
            jdbcTemplate.update("update REVIEW set USEFUL = USEFUL + 1 where id = ?", id);
        } else if (!checkReview.getBoolean("is_like")) {
            String sqlInsertLikes = "update REVIEW_LIKES set is_like = ? where review_id = ? and user_id = ?" + "values (?, ?, ?)";
            jdbcTemplate.update(sqlInsertLikes, true, id, userId);
            jdbcTemplate.update("update REVIEW set USEFUL = USEFUL + 2 where id = ?", id);
        }
        return review;
    }

    public Review dislikeReview(Integer id, Integer userId) {
        Review review = getReviewById(id);
        String sqlCheckReview = "select * from REVIEW_LIKES where review_id = ? and user_id = ?";
        SqlRowSet checkReview = jdbcTemplate.queryForRowSet(sqlCheckReview, id, userId);
        if (!checkReview.next()) {
            String sqlInsertLikes = "insert into REVIEW_LIKES (review_id, user_id, is_like) " + "values (?, ?, ?)";
            jdbcTemplate.update(sqlInsertLikes, id, userId, false);
            jdbcTemplate.update("update REVIEW set USEFUL = USEFUL - 1 where id = ?", id);
        } else if (checkReview.getBoolean("is_like")) {

            String sqlInsertLikes = "update REVIEW_LIKES set is_like = ? where review_id = ? and user_id = ?" + "values (?, ?, ?)";
            jdbcTemplate.update(sqlInsertLikes, false, id, userId);
            jdbcTemplate.update("update REVIEW set USEFUL = USEFUL - 2 where id = ?", id);
        }
        return review;
    }

    public Review deleteLike(Integer id, Integer userId) {
        Review review = getReviewById(id);
        String sqlCheckReview = "select * from REVIEW_LIKES where review_id = ? and user_id = ?";
        SqlRowSet checkReview = jdbcTemplate.queryForRowSet(sqlCheckReview, id, userId);
        if (checkReview.next() && checkReview.getBoolean("is_like")) {
            String sqlInsertLikes = "delete from REVIEW_LIKE where review_id = ? ";
            jdbcTemplate.update(sqlInsertLikes, id);
            jdbcTemplate.update("update REVIEW set USEFUL = USEFUL - 1 where id = ?", id);
        }
        return review;
    }

    public Review deleteDislike(Integer id, Integer userId) {
        Review review = getReviewById(id);
        String sqlCheckReview = "select * from REVIEW_LIKES where review_id = ? and user_id = ?";
        SqlRowSet checkReview = jdbcTemplate.queryForRowSet(sqlCheckReview, id, userId);
        if (checkReview.next() && !checkReview.getBoolean("is_like")) {
            String sqlInsertLikes = "delete from REVIEW_LIKES where review_id = ?";
            jdbcTemplate.update(sqlInsertLikes, id);
            jdbcTemplate.update("update REVIEW set USEFUL = USEFUL + 1 where id = ?", id);
        }
        return review;
    }
}
