package hotel.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hotel.app.HotelApplication;
import hotel.user.Role;
import hotel.user.UserRepository;
import hotel.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Tests the rules in SecurityConfig with real users in the H2 test database. */
@SpringBootTest(classes = HotelApplication.class)
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository users;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        users.deleteAll();
        userService.createIfMissing("admin", "admin-pass1", Role.ADMIN);
        userService.createIfMissing("rita", "reception1", Role.RECEPTIONIST);
        userService.signUp("anna", "secret123");
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void wrongRoleGets403OnAdmin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
    }

    @Test
    void loginRedirectsByRole() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("admin-pass1"))
                .andExpect(redirectedUrl("/admin"));
        mvc.perform(formLogin("/login").user("rita").password("reception1"))
                .andExpect(redirectedUrl("/reception"));
        mvc.perform(formLogin("/login").user("anna").password("secret123"))
                .andExpect(redirectedUrl("/customer"));
    }

    @Test
    void rootRedirectsToStartPageOrLogin() throws Exception {
        mvc.perform(get("/").with(user("rita").roles("RECEPTIONIST")))
                .andExpect(redirectedUrl("/reception"));
        mvc.perform(get("/")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void wrongPasswordGoesBackToLogin() throws Exception {
        mvc.perform(formLogin("/login").user("anna").password("wrong-password"))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void startPageShowsUserAndRole() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("admin")))
                .andExpect(content().string(containsString("ADMIN")))
                .andExpect(content().string(containsString("Logout")));
    }

    @Test
    void signupWithTakenNameShowsError() throws Exception {
        mvc.perform(post("/signup").param("username", "anna").param("password", "other-pass1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Username is already taken")));
    }

    @Test
    void apiNeedsAdminWithHttpBasic() throws Exception {
        mvc.perform(get("/api/rooms")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/rooms").with(httpBasic("anna", "secret123"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/rooms").with(httpBasic("admin", "admin-pass1"))).andExpect(status().isOk());
    }
}
