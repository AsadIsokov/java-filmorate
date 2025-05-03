package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {
    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
    }

    @Test
    void shouldAcceptValidFilm() {
        Film addedFilm = filmController.addFilm(validFilm);
        assertNotNull(addedFilm);
        assertTrue(addedFilm.getId() > 0);
    }

    @Test
    void shouldRejectEmptyName() {
        Film film = validFilm.toBuilder().name("").build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));
        assertTrue(exception.getMessage().contains("Название не может быть пустым"));
    }

    @Test
    void shouldRejectBlankName() {
        Film film = validFilm.toBuilder().name("   ").build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));
        assertTrue(exception.getMessage().contains("Название не может быть пустым"));
    }

    @Test
    void shouldRejectTooLongDescription() {
        String longDescription = "a".repeat(201);
        Film film = validFilm.toBuilder().description(longDescription).build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));
        assertTrue(exception.getMessage().contains("Максимальная длина описания должна быть меньше 200 символов"));
    }

    @Test
    void shouldAcceptMaxLengthDescription() {
        String maxLengthDescription = "a".repeat(200);
        Film film = validFilm.toBuilder().description(maxLengthDescription).build();

        Film addedFilm = filmController.addFilm(film);
        assertNotNull(addedFilm);
        assertEquals(200, addedFilm.getDescription().length());
    }

    @Test
    void shouldRejectTooEarlyReleaseDate() {
        Film film = validFilm.toBuilder()
                .releaseDate(LocalDate.of(1895, 12, 27))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));
        assertTrue(exception.getMessage().contains("Дата релиза должна быть не раньше 28.12.1895 года"));
    }

    @Test
    void shouldAcceptMinReleaseDate() {
        Film film = validFilm.toBuilder()
                .releaseDate(LocalDate.of(1895, 12, 28))
                .build();

        Film addedFilm = filmController.addFilm(film);
        assertNotNull(addedFilm);
        assertEquals(LocalDate.of(1895, 12, 28), addedFilm.getReleaseDate());
    }

    @Test
    void shouldRejectNegativeDuration() {
        Film film = validFilm.toBuilder().duration(-1).build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));
        assertTrue(exception.getMessage().contains("Продолжительность фильма должна быть положительной"));
    }
}
