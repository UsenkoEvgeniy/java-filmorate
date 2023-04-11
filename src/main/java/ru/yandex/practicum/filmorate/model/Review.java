package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class Review {
    Integer reviewId;
    @NotNull
    @NotBlank
    String content;

    @NotNull
    Boolean isPositive;

    @NotNull
    Integer userId;
    @NotNull
    Integer filmId;
    Integer useful;
}
