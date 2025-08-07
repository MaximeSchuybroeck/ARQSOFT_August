package pt.psoft.g1.psoftg1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestSecurityBeansConfig.class)
class PsoftG1ApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true);
	}

}
