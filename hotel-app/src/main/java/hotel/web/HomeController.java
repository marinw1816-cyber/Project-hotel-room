package hotel.web;

import hotel.security.SecurityConfig;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Start page per role. Who may open which page is set in SecurityConfig. */
@Controller
public class HomeController {

    /** GET / -> start page of the own role. Without login Spring Security sends to /login first. */
    @GetMapping("/")
    public String home(Authentication authentication) {
        return "redirect:" + SecurityConfig.startPage(authentication);
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/reception")
    public String reception() {
        return "reception";
    }

    @GetMapping("/customer")
    public String customer() {
        return "customer";
    }
}
