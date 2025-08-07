package pt.psoft.g1.psoftg1;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@TestConfiguration
public class TestSecurityBeansConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        // Return an empty stub so Spring Security is satisfied
        return (registrationId) -> null;
    }
}