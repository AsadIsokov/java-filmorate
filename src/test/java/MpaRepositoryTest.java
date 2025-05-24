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
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.repository.mpa.MpaRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaRepository.class, MpaRowMapper.class, FilmRepository.class})
@ContextConfiguration(classes = FilmorateApplication.class)
public class MpaRepositoryTest {

    private final MpaRepository mpaRepository;
    private final FilmRepository filmRepository;
    private long testFilm1Id;


    @BeforeEach
    public void setUp() {
        Mpa mpa1 = Mpa.builder()
                .id(1)
                .name("G")
                .build();
        Mpa mpa2 = Mpa.builder()
                .id(2)
                .name("PG")
                .build();
        Mpa mpa3 = Mpa.builder()
                .id(3)
                .name("PG-13")
                .build();
        Film testFilm1 = Film.builder()
                .name("Test Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(1966, 6, 6))
                .duration(120)
                .mpa(mpa2)
                .build();

        testFilm1Id = filmRepository.saveFilm(testFilm1).getId();
    }

    @Test
    public void getAll_ShouldReturnAllMpaRatings() {
        List<Mpa> mpaList = mpaRepository.getAll();

        assertNotNull(mpaList);
        assertFalse(mpaList.isEmpty());
        assertTrue(mpaList.size() >= 3);
    }

    @Test
    public void getById_WithExistingId_ShouldReturnMpa() {
        int existingId = 1;
        Mpa mpa = mpaRepository.getById(existingId);

        assertNotNull(mpa);
        assertEquals(existingId, mpa.getId());
        assertEquals("G", mpa.getName());
    }

    @Test
    public void getById_WithExistingId2_ShouldReturnMpa() {
        int existingId = 2;
        Mpa mpa = mpaRepository.getById(existingId);

        assertNotNull(mpa);
        assertEquals(existingId, mpa.getId());
        assertEquals("PG", mpa.getName());
    }

    @Test
    public void getById_WithNonExistingId_ShouldThrowNotFoundException() {
        int nonExistingId = 999;

        assertThrows(NotFoundException.class, () -> mpaRepository.getById(nonExistingId));
    }

    @Test
    public void getAll_ShouldContainStandardMpaRatings() {
        List<Mpa> mpaList = mpaRepository.getAll();

        assertTrue(mpaList.stream().anyMatch(m -> m.getName().equals("G")));
        assertTrue(mpaList.stream().anyMatch(m -> m.getName().equals("PG")));
        assertTrue(mpaList.stream().anyMatch(m -> m.getName().equals("PG-13")));
    }
}
