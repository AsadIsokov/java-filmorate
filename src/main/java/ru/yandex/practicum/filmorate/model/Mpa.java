package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

@AllArgsConstructor
@Getter
public class Mpa {
    @NonNull
    private int id;
    private String name;
}