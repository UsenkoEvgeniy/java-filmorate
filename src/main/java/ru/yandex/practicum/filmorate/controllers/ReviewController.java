package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<Review> getUsefulReviews(@RequestParam(required = false, defaultValue = "0") Integer filmId,
                                         @RequestParam(required = false, defaultValue = "10") Integer count) {
        if (filmId == 0) {
            return reviewService.getReviews();
        }
        return reviewService.getUsefulReviews(filmId, count);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Integer id) {
        return reviewService.getReviewById(id);
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public Integer deleteReview(@PathVariable Integer id) {
        return reviewService.deleteReview(id);
    }


    @PutMapping("/{id}/like/{userId}")
    public Review likeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        return reviewService.likeReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review dislikeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        return reviewService.dislikeReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Review deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        return reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Review deleteDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        return reviewService.deleteDislike(id, userId);
    }

}
