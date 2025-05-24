import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.FilmorateApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@ActiveProfiles("test")
@ContextConfiguration(classes = FilmorateApplication.class)
class FilmorateApplicationTests {
    @Test
    void contextLoads() {
        assertTrue(true, "Context loaded successfully");
    }
}