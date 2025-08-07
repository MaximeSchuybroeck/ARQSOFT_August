package pt.psoft.g1.psoftg1.auth.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import pt.psoft.g1.psoftg1.usermanagement.api.UserView;
import pt.psoft.g1.psoftg1.usermanagement.api.UserViewMapper;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.CreateUserRequest;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthApiTest {

    @InjectMocks
    private AuthApi authApi;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private UserViewMapper userViewMapper;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtEncoderParameters jwtEncoderParameters;

    @Mock
    private Jwt jwt;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword2");
    }

    @Test
    void login_shouldReturnTokenAndUserView_whenCredentialsAreCorrect() {
        AuthRequest request = new AuthRequest("testuser", "password");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("example.io")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(36000L))
                .subject("1,testuser")
                .claim("roles", "ROLE_USER")
                .build();

        when(jwtEncoder.encode(any())).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("mocked-jwt-token");

        UserView userView = new UserView(); // Fill with expected fields
        when(userViewMapper.toUserView(user)).thenReturn(userView);

        ResponseEntity<UserView> response = authApi.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mocked-jwt-token", response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals(userView, response.getBody());
    }

    @Test
    void login_shouldReturnUnauthorized_whenBadCredentials() {
        AuthRequest request = new AuthRequest("invalid", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<UserView> response = authApi.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void register_shouldReturnUserView() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("newuser");
        createUserRequest.setPassword("password");

        when(userService.create(createUserRequest)).thenReturn(user);

        UserView userView = new UserView(); // Fill with expected fields
        when(userViewMapper.toUserView(user)).thenReturn(userView);

        UserView result = authApi.register(createUserRequest);

        assertEquals(userView, result);
    }
}
