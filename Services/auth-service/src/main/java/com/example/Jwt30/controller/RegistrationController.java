package com.example.Jwt30.controller;

import com.example.Jwt30.model.JwtRequest;
import com.example.Jwt30.model.JwtResponse;
import com.example.Jwt30.model.UserRegistrationModel;
import com.example.Jwt30.security.JwtHelper;
import com.example.Jwt30.service.UserRegisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;

@RestController
@RequestMapping(path = "/auth")
public class RegistrationController {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager manager;
    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private UserRegisterService userRegisterService;
    private Logger logger = LoggerFactory.getLogger(RestController.class);

    @PostMapping(path = "/login")
    public ResponseEntity<JwtResponse> UserLogin(@RequestBody JwtRequest request) throws Exception {
        this.doAuthenticate(request.getEmail(),request.getPassword());
        UserDetails userDetails= userDetailsService.loadUserByUsername(request.getEmail());
        String token=this.jwtHelper.generateToken(userDetails);
        JwtResponse response=JwtResponse.builder()
                .jwtToken(token)
                .username(userDetails.getUsername()).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }
    private void doAuthenticate(String email, String password) throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        try {
            this.manager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {

            throw new Exception("Invalid Username & Password");
        }
    }
    @PostMapping(path = "/signup")
    public ResponseEntity<String> signup(@RequestBody UserRegistrationModel userRegistrationModel)
    {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userRegisterService.saveuser(userRegistrationModel));
    }

}
