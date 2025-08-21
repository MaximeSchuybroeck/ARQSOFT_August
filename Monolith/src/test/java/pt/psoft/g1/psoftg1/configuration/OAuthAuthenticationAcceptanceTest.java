package pt.psoft.g1.psoftg1.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import pt.psoft.g1.psoftg1.configuration.OAuthAuthenticationSuccessHandler;
import pt.psoft.g1.psoftg1.usermanagement.api.UserView;
import pt.psoft.g1.psoftg1.usermanagement.api.UserViewMapper;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuthAuthenticationAcceptanceTest {

    @Mock UserRepository userRepository;
    @Mock JwtEncoder jwtEncoder;
    @Mock UserViewMapper userViewMapper;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock PrintWriter writer;

    OAuthAuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        handler = new OAuthAuthenticationSuccessHandler((pt.psoft.g1.psoftg1.configuration.UserRepository) userRepository, jwtEncoder, userViewMapper);
        when(response.getWriter()).thenReturn(writer);
        Jwt jwt = mock(Jwt.class);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("jwt-xyz");
    }

    @Test
    void google_flow_creates_user_and_returns_json_token_and_user_data() throws Exception {
        var principal = new DefaultOAuth2User(
                AuthorityUtils.NO_AUTHORITIES,
                Map.of("email", "g@example.com", "name", "Google User"),
                "email"
        );
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google");

        when(userRepository.findByUsername("g@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserView view = mock(UserView.class);
        when(view.getUsername()).thenReturn("g@example.com");
        when(view.getFullName()).thenReturn("Google User");
        when(userViewMapper.toUserView(any(User.class))).thenReturn(view);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

        handler.onAuthenticationSuccess(request, response, token);

        verify(writer).write(body.capture());
        String json = body.getValue();
        assertThat(json).contains("jwt-xyz");
        assertThat(json).contains("g@example.com");
        assertThat(json).contains("Google User");
    }
}
