package hotel.web;

import hotel.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Login and sign-up pages. POST /login and POST /logout are handled by Spring Security. */
@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    /** Creates a CUSTOMER; on an error the form is shown again with the message. */
    @PostMapping("/signup")
    public String signup(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            userService.signUp(username, password);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "signup";
        }
        return "redirect:/login?signedUp";
    }
}
