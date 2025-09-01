package com.example.Jwt30.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/landing")
    public String landingPage() {
        return "landing"; // will resolve to landing.html
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register"; // Thymeleaf: templates/register.html
    }
}
