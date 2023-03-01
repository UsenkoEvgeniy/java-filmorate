package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    public static final LocalDate START_DATE = LocalDate.of(1895, 12, 28);
    private Integer id;
    @NotBlank(message = "Name can't be null or empty")
    private final String name;
    @Size(max = 200, message = "Description must be no more than 200 symbols")
    private final String description;
    private final LocalDate releaseDate;
    @Min(value = 1L, message = "The duration must be positive")
    private final Integer duration;
    private Set<Integer> likes = new HashSet<>();

    @AssertTrue(message = "releaseDate is before 1895.12.28")
    public boolean isReleaseDateAfter() {
        return releaseDate.isAfter(START_DATE);
    }
}
