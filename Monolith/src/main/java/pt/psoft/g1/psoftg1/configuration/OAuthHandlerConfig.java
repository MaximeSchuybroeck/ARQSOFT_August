package pt.psoft.g1.psoftg1.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import pt.psoft.g1.psoftg1.usermanagement.api.UserViewMapper;

@Configuration
public class OAuthHandlerConfig {

    @Bean
    public OAuthAuthenticationSuccessHandler oAuthAuthenticationSuccessHandler(UserRepository userRepository,
                                                                               JwtEncoder jwtEncoder,
                                                                               UserViewMapper userViewMapper) {
        return new OAuthAuthenticationSuccessHandler(userRepository, jwtEncoder, userViewMapper);
    }
}
