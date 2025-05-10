package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> allFilms();

    Film addFilm(Film film) throws ValidationException;

    Film updateFilm(Film newFilm) throws ValidationException;

    Optional<Film> getFilmById(Long id);
}
