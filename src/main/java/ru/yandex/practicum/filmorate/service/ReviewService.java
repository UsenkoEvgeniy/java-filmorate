package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperations;
import ru.yandex.practicum.filmorate.model.event.EventTypes;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewDbStorage;
    private final FilmService filmService;
    private final UserService userService;
    private final EventService eventService;


    public List<Review> getReviews() {
        log.debug("Got list of reviews");
        return reviewDbStorage.getReviews();
    }

    public Review getReviewById(Long id) {
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
        eventService.addEvent(Event.builder()
                .userId(review.getUserId())
                .entityId(review.getReviewId())
                .eventType(EventTypes.REVIEW.name())
                .operation(EventOperations.ADD.name())
                .timestamp(Instant.now().toEpochMilli())
                .build());
        log.debug("Review added");
        return review;
    }

    public Review updateReview(Review review) {
        userService.getUserById(review.getUserId());
        filmService.getFilmById(review.getFilmId());
        log.debug("Review updated");
        Review reviewUpdate = reviewDbStorage.updateReview(review);
        eventService.addEvent(Event.builder()
                .userId(reviewUpdate.getUserId())
                .entityId(reviewUpdate.getReviewId())
                .eventType(EventTypes.REVIEW.name())
                .operation(EventOperations.UPDATE.name())
                .timestamp(Instant.now().toEpochMilli())
                .build());
        return reviewUpdate;
    }

    public Boolean deleteReview(Long id) {
        log.debug("Review deleted");
        eventService.addEvent(Event.builder()
                .userId(reviewDbStorage.getReviewById(id).getUserId())
                .entityId(id)
                .eventType(EventTypes.REVIEW.name())
                .operation(EventOperations.REMOVE.name())
                .timestamp(Instant.now().toEpochMilli())
                .build());
        return reviewDbStorage.deleteReview(id);
    }

    public List<Review> getUsefulReviews(Long filmId, Long count) {
        return reviewDbStorage.getMostUsefulReviews(filmId, count);
    }

    public Review likeReview(Long id, Long userId) {
        log.debug("Review liked");
        return reviewDbStorage.likeReview(id, userId);
    }

    public Review dislikeReview(Long id, Long userId) {
        log.debug("Review disliked");
        return reviewDbStorage.dislikeReview(id, userId);
    }

    public Review deleteLike(Long id, Long userId) {
        log.debug("Review like deleted");
        eventService.addEvent(Event.builder()
                .userId(userId)
                .entityId(id)
                .eventType(EventTypes.LIKE.name())
                .operation(EventOperations.REMOVE.name())
                .timestamp(Instant.now().toEpochMilli())
                .build());
        return reviewDbStorage.deleteLike(id, userId);
    }

    public Review deleteDislike(Long id, Long userId) {
        log.debug("Review dislike deleted");
        return reviewDbStorage.deleteDislike(id, userId);
    }
}
