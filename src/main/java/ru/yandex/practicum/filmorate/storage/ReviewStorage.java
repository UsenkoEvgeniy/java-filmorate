package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    List<Review> getReviews();

    Review getReviewById(Long id);

    Review addReview(Review review);

    Review updateReview(Review review);

    Boolean deleteReview(Long id);

    List<Review> getMostUsefulReviews(Long filmId, Long count);

    Review likeReview(Long id, Long userId);

    Review dislikeReview(Long id, Long userId);

    Review deleteLike(Long id, Long userId);

    Review deleteDislike(Long id, Long userId);

}
