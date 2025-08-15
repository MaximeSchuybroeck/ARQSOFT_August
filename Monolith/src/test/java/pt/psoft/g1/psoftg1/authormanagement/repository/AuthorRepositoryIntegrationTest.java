package pt.psoft.g1.psoftg1.authormanagement.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Based on https://www.baeldung.com/spring-boot-testing
 * <p>Adaptations to Junit 5 with ChatGPT
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthorRepositoryIntegrationTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    public void whenFindByName_thenReturnAuthor() {
        Author alex = new Author("Alex", "O Alex escreveu livros", null);
        entityManager.persist(alex);
        entityManager.flush();

        List<Author> list = authorRepository.searchByNameName(alex.getName());

        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getName()).isEqualTo(alex.getName());
    }
}

