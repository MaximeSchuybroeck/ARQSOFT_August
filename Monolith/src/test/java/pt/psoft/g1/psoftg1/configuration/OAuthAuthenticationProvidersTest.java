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
import pt.psoft.g1.psoftg1.usermanagement.api.UserView;
import pt.psoft.g1.psoftg1.usermanagement.api.UserViewMapper;
import pt.psoft.g1.psoftg1.usermanagement.model.User;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuthAuthenticationProvidersTest {

    @Mock UserRepository userRepository;
    @Mock JwtEncoder jwtEncoder;
    @Mock UserViewMapper userViewMapper;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock PrintWriter writer;

    OAuthAuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        handler = new OAuthAuthenticationSuccessHandler(userRepository, jwtEncoder, userViewMapper);
        when(response.getWriter()).thenReturn(writer);
        Jwt jwt = mock(Jwt.class);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("jwt-xyz");
    }

    @Test
    void facebookRegistration_isHandled_caseInsensitive_andNoProvisioningWhenExists() throws Exception {
        var principal = new DefaultOAuth2User(AuthorityUtils.NO_AUTHORITIES, Map.of(
                "email", "f@example.com",
                "name", "Facebook User"
        ), "email");
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "FACEBOOK");

        when(userRepository.findByUsername("f@example.com")).thenReturn(Optional.of(new User()));
        when(userViewMapper.toUserView(any(User.class))).thenReturn(mock(UserView.class));

        handler.onAuthenticationSuccess(request, response, token);

        verify(userRepository, never()).save(any());
        verify(writer).write(anyString());
    }

    @Test
    void githubRegistration_isHandled_likeOtherProviders() throws Exception {
        var principal = new DefaultOAuth2User(AuthorityUtils.NO_AUTHORITIES, Map.of(
                "email", "gh@example.com",
                "name", "GitHub User"
        ), "email");
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "github");

        when(userRepository.findByUsername("gh@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userViewMapper.toUserView(any(User.class))).thenReturn(mock(UserView.class));

        handler.onAuthenticationSuccess(request, response, token);

        verify(userRepository).save(any(User.class));
        verify(writer).write(anyString());
    }
}

