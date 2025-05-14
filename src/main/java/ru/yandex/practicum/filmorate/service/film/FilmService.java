package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    FilmDto saveFilm(Film film);

    FilmDto updateFilm(Film film);

    void addLike(Long userId, Long filmId);

    void removeLike(Long userId, Long filmId);

    List<FilmDto> getTheMostPopularFilms(Integer count);

    FilmDto getFilmById(Long filmId);

    List<FilmDto> getAllFilms();
}