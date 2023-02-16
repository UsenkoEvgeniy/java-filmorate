package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FilmorateApplicationTests {

	static Validator validator;
	@BeforeAll
	public static void setupValidatorInstance() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Nested
	class UserValidationTest{
		final String email = "correct@email.com";
		final String login = "CorrectLogin";
		final LocalDate birthDay = LocalDate.of(2000, 1, 1);
		@Test
		void addUserEmptyEmail() {
			User user = new User("", login, birthDay);
			Set<ConstraintViolation<User>> violationSet = validator.validate(user);
			assertEquals(1, violationSet.size());
			assertEquals("Email can't be null or empty", violationSet.iterator().next().getMessage());
		}

		@Test
		void addUserIncorrectEmail() {
			User user = new User("my.email.ru", login, birthDay);
			Set<ConstraintViolation<User>> violationSet = validator.validate(user);
			assertEquals(1, violationSet.size());
			assertEquals("Incorrect email", violationSet.iterator().next().getMessage());
		}

		@Test
		void addUserEmptyLogin() {
			User user = new User(email, "", birthDay);
			Set<ConstraintViolation<User>> violationSet = validator.validate(user);
			assertEquals(1, violationSet.size());
			assertEquals("No whitespaces allowed", violationSet.iterator().next().getMessage());
		}

		@Test
		void addUserLoginWithSpaces() {
			User user = new User(email, "lo gin", birthDay);
			Set<ConstraintViolation<User>> violationSet = validator.validate(user);
			assertEquals(1, violationSet.size());
			assertEquals("No whitespaces allowed", violationSet.iterator().next().getMessage());
		}

		@Test
		void addUserEmptyName() {
			User user = new User(email, login, birthDay);
			Set<ConstraintViolation<User>> violationSet = validator.validate(user);
			assertEquals(0, violationSet.size());
			assertEquals(user.getName(), user.getLogin(), "Name is not equal login");
		}

		@Test
		void addUserBirthdayInFuture() {
			User user = new User(email, login, LocalDate.now().plusDays(1));
			Set<ConstraintViolation<User>> violationSet = validator.validate(user);
			assertEquals(1, violationSet.size());
			assertEquals("Birthday must be in past", violationSet.iterator().next().getMessage());
		}

		@Test
		void addUserWithoutErrors() {
			User user = new User(email, login, birthDay);
			user.setName("name");
			Set<ConstraintViolation<User>> violationSet = validator.validate(user);
			assertEquals(0, violationSet.size());
		}
	}

	@Nested
	class FilmValidationTest{
		final LocalDate correctDate = LocalDate.of(2000, 4, 4);
		final String name = "Correct name";
		final String description = "Correct description";
		final Integer duration = 30;

		@Test
		void createFilmEmptyName() {
			Film film = new Film("", description, correctDate, duration);
			Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
			assertEquals(1, violationSet.size());
			assertEquals("Name can't be null or empty", violationSet.iterator().next().getMessage());
		}

		@Test
		void createFilmNullName() {
			Film film = new Film(null, description, correctDate, duration);
			Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
			assertEquals(1, violationSet.size());
			assertEquals("Name can't be null or empty", violationSet.iterator().next().getMessage());
		}

		@Test
		void createFilmWithDescriptionOver200() {
			Film film = new Film(name, "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. " +
					"Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient " +
					"montes, nascetur ridiculus mus. Donec qua", correctDate, 30);
			Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
			assertEquals(1, violationSet.size());
			assertEquals("Description must be no more than 200 symbols", violationSet.iterator().next().getMessage());
		}

		@Test
		void createFilmTooOld() {
			Film film = new Film(name, description, Film.START_DATE.minusDays(1), duration);
			Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
			assertEquals(1, violationSet.size());
			assertEquals("releaseDate is before 1895.12.28", violationSet.iterator().next().getMessage());
		}

		@Test
		void createFilmWith0Duration() {
			Film film = new Film(name, description, correctDate, 0);
			Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
			assertEquals(1, violationSet.size());
			assertEquals("The duration must be positive", violationSet.iterator().next().getMessage());
		}

		@Test
		void addFilmWithoutErrors() {
			Film film = new Film(name, description, correctDate, duration);
			Set<ConstraintViolation<Film>> violationSet = validator.validate(film);
			assertEquals(0, violationSet.size());
		}
	}

}
