import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.film.FilmRepositoryInterface;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.repository.mpa.MpaRepository;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;
import ru.yandex.practicum.filmorate.repository.user.UserRepositoryInterface;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@Import({
        FilmRepository.class,
        UserRepository.class,
        MpaRepository.class,
        FilmRowMapper.class,
        UserRowMapper.class
})
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class FilmRepositoryTest {
    private final FilmRepositoryInterface filmRepositoryInterface;
    private final UserRepositoryInterface userRepositoryInterface;
    private Long firstFilmId;
    private Long secondFilmId;
    private Long testUserId;

    @BeforeEach
    void prepareTestEnvironment() {
        initTestFilms();
        initTestUser();
    }

    private void initTestFilms() {
        Film firstFilm = Film.builder()
                .name("First Test Movie")
                .description("Initial plot for first film")
                .releaseDate(LocalDate.of(2001, 4, 13))
                .duration(115)
                .mpa(new Mpa(4, "R"))
                .build();

        Film secondFilm = Film.builder()
                .name("Second Test Movie")
                .description("Initial plot for second film")
                .releaseDate(LocalDate.of(1898, 11, 3))
                .duration(85)
                .mpa(new Mpa(1, "G"))
                .build();

        firstFilmId = filmRepositoryInterface.saveFilm(firstFilm).getId();
        secondFilmId = filmRepositoryInterface.saveFilm(secondFilm).getId();
    }

    private void initTestUser() {
        User secondUser = User.builder()
                .email("user2@test.org")
                .login("second-user")
                .name("Second Test User")
                .birthday(LocalDate.of(1890, 6, 12))
                .build();

        testUserId = userRepositoryInterface.saveUser(secondUser).getId();
    }

    @Test
    void newFilmShouldBePersistedCorrectly() {
        Film newFilm = Film.builder()
                .name("Fresh Movie")
                .description("Brand new movie plot")
                .releaseDate(LocalDate.of(2020, 2, 20))
                .duration(105)
                .mpa(new Mpa(4, "R"))
                .build();

        Film film3 = filmRepositoryInterface.saveFilm(newFilm);

        assertAll(
                () -> assertThat(film3.getId()).isPositive(),
                () -> assertEquals("Fresh Movie", film3.getName()),
                () -> assertEquals("Brand new movie plot", film3.getDescription()),
                () -> assertEquals(LocalDate.of(2020, 2, 20), film3.getReleaseDate()),
                () -> assertEquals(105, film3.getDuration()),
                () -> assertEquals(4, film3.getMpa().getId())
        );
    }

    @Test
    void filmShouldBeFullyUpdated() {
        Film existingFilm = filmRepositoryInterface.getFilmById(firstFilmId).orElseThrow();

        Film updatedVersion = Film.builder()
                .id(existingFilm.getId())
                .name("Modified Title")
                .description("Updated plot description")
                .releaseDate(existingFilm.getReleaseDate())
                .duration(existingFilm.getDuration())
                .mpa(existingFilm.getMpa())
                .build();

        Film result = filmRepositoryInterface.updateFilm(updatedVersion);

        assertAll(
                () -> assertEquals("Modified Title", result.getName()),
                () -> assertEquals("Updated plot description", result.getDescription()),
                () -> assertEquals(existingFilm.getReleaseDate(), result.getReleaseDate()),
                () -> assertEquals(existingFilm.getDuration(), result.getDuration()),
                () -> assertEquals(existingFilm.getMpa(), result.getMpa())
        );
    }

    @Test
    void shouldRetrieveExistingFilmById() {
        Optional<Film> filmOptional = filmRepositoryInterface.getFilmById(secondFilmId);

        assertThat(filmOptional)
                .isPresent()
                .get()
                .satisfies(film -> {
                    assertEquals("Second Test Movie", film.getName());
                    assertEquals(85, film.getDuration());
                });
    }

    @Test
    void shouldFetchAllAvailableFilms() {
        Collection<Film> allFilms = filmRepositoryInterface.getAllFilms();

        assertThat(allFilms)
                .hasSize(2)
                .extracting(Film::getId)
                .containsExactlyInAnyOrder(firstFilmId, secondFilmId);
    }

    @Test
    void filmShouldGainPopularityAfterLike() {
        List<Film> initiallyPopular = filmRepositoryInterface.getTheMostPopularFilms(1);
        assertThat(initiallyPopular).extracting(Film::getId).doesNotContain(firstFilmId);
        filmRepositoryInterface.addLike(firstFilmId, testUserId);

        List<Film> popularAfterLike = filmRepositoryInterface.getTheMostPopularFilms(1);
        assertThat(popularAfterLike)
                .extracting(Film::getId)
                .contains(firstFilmId);
    }

    @Test
    void likeRemovalShouldAffectPopularity() {
        filmRepositoryInterface.addLike(firstFilmId, testUserId);
        assertThat(filmRepositoryInterface.getLikes(firstFilmId)).hasSize(1);
        filmRepositoryInterface.removeLike(firstFilmId, testUserId);
        assertThat(filmRepositoryInterface.getLikes(firstFilmId)).isEmpty();
        List<Film> topAfterRemoval = filmRepositoryInterface.getTheMostPopularFilms(2);
        assertThat(topAfterRemoval).extracting(Film::getId).doesNotContain(firstFilmId);
    }
}
