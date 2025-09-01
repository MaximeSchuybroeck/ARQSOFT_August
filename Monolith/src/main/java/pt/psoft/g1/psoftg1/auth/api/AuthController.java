package pt.psoft.g1.psoftg1.auth.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // will resolve to login.html
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
