package ru.yandex.practicum.filmorate.repository.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreRepositoryInterface {
    List<Genre> getAllGenres();

    Genre getById(int id);

    List<Genre> findByFilmId(Long filmId);
}