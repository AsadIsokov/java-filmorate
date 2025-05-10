package ru.yandex.practicum.filmorate.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorResponse {
    private final String error;
}
