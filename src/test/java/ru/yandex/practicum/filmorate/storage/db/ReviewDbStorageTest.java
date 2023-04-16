package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewDbStorageTest {

    final ReviewDbStorage storage;
    final FilmDbStorage storageFilm;
    final UserDbStorage storageUser;

    @BeforeEach
    void setUp() {
        Film film = new Film("Test", "Test", LocalDate.now(), 100);
        film.setMpa(new Mpa(1, null));
        storageFilm.addFilm(film);
        User user = new User("user@user.ru", "user", LocalDate.now());
        user.setName("user");
        storageUser.addUser(user);
        Review review = Review.builder().content("TEST1")
                .isPositive(true).userId(1L).filmId(1L).build();
        Review review1 = Review.builder().content("TEST2")
                .isPositive(false).userId(1L).filmId(1L).build();
        Review review2 = Review.builder().content("TEST2")
                .isPositive(false).userId(1L).filmId(1L).build();
        storage.addReview(review);
        storage.addReview(review1);
        storage.addReview(review2);
    }

    @Test
    void getReviews() {
        assertEquals("", 3, storage.getReviews().size());
        assertEquals("", storage.getReviews().get(0).getReviewId(), 1L);
        assertEquals("", storage.getReviews().get(0).getContent(), "TEST1");
        assertTrue("", storage.getReviews().get(0).getIsPositive());
        assertEquals("", storage.getReviews().get(0).getFilmId(), 1L);
        assertEquals("", storage.getReviews().get(0).getUserId(), 1L);
    }

    @Test
    void getReviewById() {
        assertEquals("", storage.getReviewById(1L).getReviewId(), 1L);
        assertEquals("", storage.getReviewById(1L).getContent(), "TEST1");
        assertTrue("", storage.getReviewById(1L).getIsPositive());
        assertEquals("", storage.getReviewById(1L).getFilmId(), 1L);
        assertEquals("", storage.getReviewById(1L).getUserId(), 1L);
    }

    @Test
    void updateReview() {
        Review review = storage.getReviewById(2L);
        review.setContent("TEST3");
        review.setIsPositive(true);
        review.setUserId(2L);
        review.setFilmId(2L);
        storage.updateReview(review);
        assertEquals("", storage.getReviewById(2L).getReviewId(), 2L);
        assertEquals("", storage.getReviewById(2L).getContent(), "TEST3");
        assertTrue("", storage.getReviewById(2L).getIsPositive());
        assertEquals("", storage.getReviewById(2L).getFilmId(), 1L);
        assertEquals("", storage.getReviewById(2L).getUserId(), 1L);
    }

    @Test
    void deleteReview() {
        int sizeTest = storage.getReviews().size();
        storage.deleteReview(2L);
        int checkSize = storage.getReviews().size();
        assertTrue("", sizeTest > checkSize);
        assertTrue("", sizeTest - 1 == checkSize);
        assertThrows(NotFoundException.class, () -> storage.getReviewById(2L));

    }

    @Test
    void getMostUsefulReviews() {
        storage.likeReview(1L, 1L);
        assertTrue("", storage.getMostUsefulReviews(1L, 3L).size() <= 3);
        assertEquals("", storage.getMostUsefulReviews(1L, 3L).get(0).getUseful(), 1);
        assertEquals("", storage.getMostUsefulReviews(1L, 3L).get(0).getReviewId(), 1L);
        assertEquals("", storage.getMostUsefulReviews(1L, 3L).get(0).getContent(), "TEST1");
        assertEquals("", storage.getMostUsefulReviews(1L, 3L).get(0).getFilmId(), 1L);
    }

    @Test
    void dislikeReview() {
        storage.dislikeReview(3L, 1L);
        assertEquals("", storage.getReviewById(3L).getUseful(), -1);
    }

    @Test
    void deleteLike() {
        storage.likeReview(1L, 1L);
        storage.deleteLike(1L, 1L);
        assertEquals("", storage.getReviewById(1L).getUseful(), 0);
    }

    @Test
    void deleteDislike() {
        storage.dislikeReview(3L, 1L);
        storage.deleteDislike(3L, 1L);
        assertEquals("", storage.getReviewById(3L).getUseful(), 0);
    }
}