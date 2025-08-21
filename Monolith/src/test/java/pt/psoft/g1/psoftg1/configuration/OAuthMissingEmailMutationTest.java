package pt.psoft.g1.psoftg1.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import pt.psoft.g1.psoftg1.configuration.OAuthAuthenticationSuccessHandler;
import pt.psoft.g1.psoftg1.usermanagement.api.UserViewMapper;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.io.PrintWriter;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuthMissingEmailMutationTest {

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
    }

    @Test
    void missingEmail_returnsBadRequest_andPreventsTokenIssueOrPersistence() throws Exception {
        var principal = new DefaultOAuth2User(
                AuthorityUtils.NO_AUTHORITIES,
                Map.of("name", "No Email"), // geen "email" attribuut
                "name"
        );
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google");

        handler.onAuthenticationSuccess(request, response, token);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
        verifyNoInteractions(userRepository, userViewMapper, jwtEncoder); // waarom: must fail fast zonder bijwerking
        verify(writer, never()).write(anyString());
    }
}

