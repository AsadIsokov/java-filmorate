package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
@Builder
public class Genre {
    @NonNull
    private Integer id;
    private String name;
}