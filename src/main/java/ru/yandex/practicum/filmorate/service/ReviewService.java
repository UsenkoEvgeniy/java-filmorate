package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.db.ReviewDbStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewDbStorage reviewDbStorage;
    private final FilmService filmService;
    private final UserService userService;


    public List<Review> getReviews() {
        log.debug("Got list of reviews");
        return reviewDbStorage.getReviews();
    }

    public Review getReviewById(Integer id) {
        log.debug("Got review by id");
        return reviewDbStorage.getReviewById(id);
    }

    public Review addReview(Review review) {
        if (review.getUseful() == null) {
            review.setUseful(0);
        }
        userService.getUserById(review.getUserId());
        filmService.getFilmById(review.getFilmId());
        reviewDbStorage.addReview(review);
        log.debug("Review added");
        return review;
    }

    public Review updateReview(Review review) {
        userService.getUserById(review.getUserId());
        filmService.getFilmById(review.getFilmId());
        log.debug("Review updated");
        return reviewDbStorage.updateReview(review);
    }

    public Boolean deleteReview(Integer id) {
        log.debug("Review deleted");
        return reviewDbStorage.deleteReview(id);
    }

    public List<Review> getUsefulReviews(Integer filmId, Integer count) {
        return reviewDbStorage.getMostUsefulReviews(filmId, count);
    }

    public Review likeReview(Integer id, Integer userId) {
        log.debug("Review liked");
        return reviewDbStorage.likeReview(id, userId);
    }

    public Review dislikeReview(Integer id, Integer userId) {
        log.debug("Review disliked");
        return reviewDbStorage.dislikeReview(id, userId);
    }

    public Review deleteLike(Integer id, Integer userId) {
        log.debug("Review like deleted");
        return reviewDbStorage.deleteLike(id, userId);
    }

    public Review deleteDislike(Integer id, Integer userId) {
        log.debug("Review dislike deleted");
        return reviewDbStorage.deleteDislike(id, userId);
    }
}
