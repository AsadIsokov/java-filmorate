import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmRepository.class, FilmMapper.class, UserRepository.class, UserRowMapper.class})
@ContextConfiguration(classes = FilmorateApplication.class)
public class FilmRepositoryTest {
    private final FilmRepository filmStorage;
    private final UserRepository userRepository;

    private Long testFilm1Id;
    private Long testFilm2Id;
    private Long testUserId;

    @BeforeEach
    public void setUp() {
        testFilm1Id = filmStorage.saveFilm(
                new Film(null, "Test Film 1", "Description 1",
                        LocalDate.of(1966, 6, 6), 120, List.of(), new Mpa(3, "PG-13"))
        ).getId();

        testFilm2Id = filmStorage.saveFilm(
                new Film(null, "Test Film 2", "Description 2",
                        LocalDate.of(1968, 8, 8), 90, List.of(), new Mpa(1, "G"))
        ).getId();

        testUserId = userRepository.saveUser(
                new User(null, "user@test.com", "testUser",
                        "Test User", LocalDate.of(1990, 1, 1))
        ).getId();
    }

    @Test
    public void testAddFilm() {
        Film newFilm = new Film(null, "Test Film 3", "Description 3",
                LocalDate.of(1970, 10, 10), 120, List.of(), new Mpa(3, "PG-13"));
        Film addedFilm = filmStorage.saveFilm(newFilm);

        assertThat(addedFilm).isNotNull();
        assertThat(addedFilm.getId()).isNotNull();
        assertThat(addedFilm.getName()).isEqualTo("Test Film 3");
        assertThat(addedFilm.getDescription()).isEqualTo("Description 3");
        assertThat(addedFilm.getReleaseDate()).isEqualTo(LocalDate.of(1970, 10, 10));
        assertThat(addedFilm.getDuration()).isEqualTo(120);
        assertThat(addedFilm.getMpa().getName()).isEqualTo("PG-13");
    }

    @Test
    public void testGetAllFilms() {
        Collection<Film> films = filmStorage.getAllFilms();
        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getId).containsExactlyInAnyOrder(testFilm1Id, testFilm2Id);
    }

    @Test
    public void testUpdateFilm_NotFound() {
        Long nonExistentFilmId = 999L;
        Film nonExistentFilm = new Film(nonExistentFilmId, "Non Existent Film", "nonexistentfilm",
                LocalDate.of(1967, 12, 9), 120, List.of(), new Mpa(3, "PG-13"));

        assertThatThrownBy(() -> filmStorage.updateFilm(nonExistentFilm))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм с id = 999 не найден");
    }

    @Test
    public void testAddLike() {
        filmStorage.addLike(testUserId, testFilm1Id);
        List<Film> popularFilms = filmStorage.getTheMostPopularFilms(1);
        assertThat(popularFilms).hasSize(1);
        assertThat(popularFilms.get(0).getId()).isEqualTo(testFilm1Id);
        List<Long> likes = filmStorage.getLikes(testFilm1Id);
        assertThat(likes).containsExactly(testUserId);
    }


    @Test
    public void testRemoveLike() {
        filmStorage.addLike(testUserId, testFilm1Id);
        assertThat(filmStorage.getLikes(testFilm1Id)).hasSize(1);
        filmStorage.removeLike(testUserId, testFilm1Id);
        assertThat(filmStorage.getLikes(testFilm1Id)).isEmpty();
    }

    @Test
    public void testGetTheMostPopularFilms() {
        filmStorage.addLike(testUserId, testFilm1Id);
        Long secondUserId = userRepository.saveUser(
                new User(null, "user2@test.com", "testUser2",
                        "Test User 2", LocalDate.of(1995, 5, 5))
        ).getId();
        filmStorage.addLike(secondUserId, testFilm1Id);
        filmStorage.addLike(testUserId, testFilm2Id);

        List<Film> popularFilms = filmStorage.getTheMostPopularFilms(2);
        assertThat(popularFilms).hasSize(2);

        assertThat(popularFilms.get(0).getId()).isEqualTo(testFilm1Id);
        assertThat(popularFilms.get(1).getId()).isEqualTo(testFilm2Id);

        List<Film> singlePopularFilm = filmStorage.getTheMostPopularFilms(1);
        assertThat(singlePopularFilm).hasSize(1);
        assertThat(singlePopularFilm.get(0).getId()).isEqualTo(testFilm1Id);
    }

    @Test
    public void testGetFilmById() {
        Film film = filmStorage.getFilmById(testFilm1Id).orElseThrow();

        assertThat(film.getId()).isEqualTo(testFilm1Id);
        assertThat(film.getName()).isEqualTo("Test Film 1");
        assertThat(film.getDescription()).isEqualTo("Description 1");
        assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(1966, 6, 6));
        assertThat(film.getDuration()).isEqualTo(120);
        assertThat(film.getMpa().getName()).isEqualTo("PG-13");
    }

    @Test
    public void testGetFilmById_NotFound() {
        Long nonExistentFilmId = 999L;

        assertThat(filmStorage.getFilmById(nonExistentFilmId)).isEmpty();
    }
}