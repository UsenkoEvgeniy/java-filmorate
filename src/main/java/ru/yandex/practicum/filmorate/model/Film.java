package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class Film {
    private Integer id;
    @NotBlank(message = "Name can't be null or empty")
    private final String name;
    @Size(max = 200, message = "Description must be no more than 200 symbols")
    private final String description;
    private final LocalDate releaseDate;
    @Min(value = 1L, message = "The duration must be positive")
    private final Integer duration;

    @AssertTrue(message = "releaseDate is after 1895.12.28")
    public boolean isReleaseDateAfter() {
        LocalDate baseDate = LocalDate.of(1895, 12, 28);
        return releaseDate.isAfter(baseDate);
    }
}
