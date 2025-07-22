//package tournament.usmlsa.be.code.v1.security;
//
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import tournament.usmlsa.be.code.v1.daoRepositories.PersonRepository;
//import tournament.usmlsa.be.code.v1.dtoModels.auth.AuthenticationResponse;
//import tournament.usmlsa.be.code.v1.entities.Person;
//import tournament.usmlsa.be.code.v1.enums.ProviderEnum;
//import tournament.usmlsa.be.code.v1.servicesImplementation.PersonServiceImpl;
//
//import java.io.IOException;
//import java.util.Objects;
//
//@Component
//public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
//
//    @Autowired
//    private PersonRepository userRepo;
//    @Autowired
//    @Lazy
//    private PersonServiceImpl personServiceImpl;
//
//    @Value("${frontend.server.url}")
//    private String frontendServerUrl;
//
//    @Value("${backend.server.url}")
//    private String backendServerUrl;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//
//        var oauth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
//        String authorizedClientRegistrationId = oauth2AuthenticationToken.getAuthorizedClientRegistrationId();
//        var oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
//        String email = oauthUser.getAttribute("email");
//
//        Person user = new Person();
//
//        if (authorizedClientRegistrationId.equalsIgnoreCase("google")) {
//            user.setEmail(Objects.requireNonNull(oauthUser.getAttribute("email")).toString());
//            user.setProfilePictureUrl(Objects.requireNonNull(oauthUser.getAttribute("picture")));
//            user.setFirstName(Objects.requireNonNull(oauthUser.getAttribute("name")).toString());
//            user.setProvider(String.valueOf(ProviderEnum.GOOGLE));
//
//            Person user2 = userRepo.findByEmail(user.getEmail());
//            String token;
//            AuthenticationResponse googleUserResponse = new AuthenticationResponse();
//            String redirectUrl = "";
//            if (user2 == null) {
//                user = userRepo.save(user);
/// /            new DefaultRedirectStrategy().sendRedirect(request, response, "/home/select-role");
/// /                response.setContentType("application/json");
/// /                response.setCharacterEncoding("UTF-8");
//                googleUserResponse = personServiceImpl.getGoogleUserResponse();
//                token = googleUserResponse.getAccessToken();//
/// /                response.getWriter().write(new ObjectMapper().writeValueAsString(googleUserResponse));
/// /                response.setStatus(HttpServletResponse.SC_OK);
//                redirectUrl = "http://localhost:3000/auth/select-role" + "?token=" + token;
//            } else {
/// /                response.setContentType("application/json");
/// /                response.setCharacterEncoding("UTF-8");
//                googleUserResponse = personServiceImpl.getGoogleUserResponse();
////                response.getWriter().write(new ObjectMapper().writeValueAsString(googleUserResponse));
////                response.setStatus(HttpServletResponse.SC_OK);
//                token = googleUserResponse.getAccessToken();
//                redirectUrl = frontendServerUrl + "/auth/google-succeed" + "?xbyd=" + token;
//            }
//            response.sendRedirect(redirectUrl);
//
//            /** Send token and user info as JSON response
//             //            response.setContentType("application/json");
//             //            response.setCharacterEncoding("UTF-8");
//             //            Map<String, Object> jsonResponse = new HashMap<>();
//             //            jsonResponse.put("token", token);
//             //            jsonResponse.put("role", role); // or other data needed
//             //            jsonResponse.put("google-response", googleUserResponse);
//             //            response.getWriter().write(new ObjectMapper().writeValueAsString(jsonResponse));   **/
//        }
//    }
//}
package pt.psoft.g1.psoftg1.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.usermanagement.api.UserView;
import pt.psoft.g1.psoftg1.usermanagement.api.UserViewMapper;
import pt.psoft.g1.psoftg1.usermanagement.model.User;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;
    private final UserViewMapper userViewMapper;

    public OAuthAuthenticationSuccessHandler(UserRepository userRepository, JwtEncoder jwtEncoder, UserViewMapper userViewMapper) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
        this.userViewMapper = userViewMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        String provider = oauth2Token.getAuthorizedClientRegistrationId().toUpperCase(); // Normalize and receive either "GOOGLE" or "FACEBOOK"

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        if (email == null || name == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required user attributes.");
            return;
        }

        boolean isNewUser = !userRepository.existsByUsername(email);
        User user = processOAuthPostLogin(email, name, provider);

        // Create JWT token
        Instant now = Instant.now();
        long expiry = 3600L;
        String scope = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("example.io").issuedAt(now).expiresAt(now.plusSeconds(expiry)).subject(user.getId() + "," + user.getUsername()).claim("roles", scope).build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        UserView userView = userViewMapper.toUserView(user);

//        // Decide where to redirect or respond
//        if (isNewUser) {
//            response.sendRedirect("http://localhost:3000/auth/select-role?token=" + token);
//        } else {
//            response.sendRedirect("http://localhost:3000/auth/" + provider.toLowerCase() + "-succeed?token=" + token);
//        }

        // Optional: send JSON instead of redirect
        String json = new ObjectMapper().writeValueAsString(Map.of("token", token, "id", userView.getId(), "userName", userView.getUsername(), "fullName", userView.getFullName()));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);

    }


    public User processOAuthPostLogin(String email, String name, String provider) {
        return userRepository.findByUsername(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setName(name);
            newUser.setIamProvider(provider);
            newUser.setPassword("Secure@1234");
            return userRepository.save(newUser);
        });
    }
}