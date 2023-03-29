package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.validator.UserNameConstraint;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@UserNameConstraint
public class User {
    private long id;
    @NotBlank(message = "Email can't be null or empty")
    @Email(message = "Incorrect email")
    private final String email;
    @Pattern(regexp = "\\S+", message = "No whitespaces allowed")
    private final String login;
    private String name;
    @Past(message = "Birthday must be in past")
    private final LocalDate birthday;
    private Map<Long, String> friends = new HashMap<>();
}