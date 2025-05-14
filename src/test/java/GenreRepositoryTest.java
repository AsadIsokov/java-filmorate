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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.genre.GenreRepository;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreRepository.class, GenreRowMapper.class, FilmRepository.class})
@ContextConfiguration(classes = FilmorateApplication.class)
public class GenreRepositoryTest {

    private final GenreRepository genreRepository;
    private final FilmRepository filmRepository;
    private long testFilm1Id;


    @BeforeEach
    public void setUp() {
        Genre genre1 = Genre.builder()
                .id(1)
                .name("Комедия")
                .build();
        Genre genre2 = Genre.builder()
                .id(2)
                .name("Драма")
                .build();
        Genre genre3 = Genre.builder()
                .id(3)
                .name("Мультфилм")
                .build();
        Film testFilm1 = Film.builder()
                .name("Test Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(1966, 6, 6))
                .duration(120)
                .mpa(new Mpa(3, "PG-13"))
                .genres(List.of(genre1, genre2, genre3))
                .build();

        testFilm1Id = filmRepository.saveFilm(testFilm1).getId();
    }

    @Test
    public void getAllGenres_ShouldReturnAllGenres() {
        List<Genre> genres = genreRepository.getAllGenres();

        assertNotNull(genres);
        assertFalse(genres.isEmpty());
        assertTrue(genres.size() >= 3);
    }

    @Test
    public void getById_WithExistingId_ShouldReturnGenre() {
        int existingId = 1;
        Genre genre = genreRepository.getById(existingId);

        assertNotNull(genre);
        assertEquals(existingId, genre.getId());
        assertEquals("Комедия", genre.getName());
    }

    @Test
    public void getById_WithNonExistingId_ShouldThrowNotFoundException() {
        int nonExistingId = 999;

        assertThrows(NotFoundException.class, () -> genreRepository.getById(nonExistingId));
    }

    @Test
    public void findByFilmId_ShouldReturnGenresForFilm() {
        List<Genre> genres = genreRepository.findByFilmId(testFilm1Id);

        assertNotNull(genres);
        assertEquals(3, genres.size());
        assertTrue(genres.stream().anyMatch(g -> g.getName().equals("Комедия")));
        assertTrue(genres.stream().anyMatch(g -> g.getName().equals("Драма")));
    }

    @Test
    public void findByFilmId_WithNonExistingFilmId_ShouldReturnEmptyList() {
        long nonExistingFilmId = 999L;
        List<Genre> genres = genreRepository.findByFilmId(nonExistingFilmId);

        assertNotNull(genres);
        assertTrue(genres.isEmpty());
    }
}
