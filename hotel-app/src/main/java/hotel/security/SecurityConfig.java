package hotel.security;

import java.util.Set;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Two filter chains:
 * 1. /api/**: REST API, HTTP Basic, only ADMIN, no CSRF.
 * 2. everything else: HTML pages with form login, CSRF on.
 */
@Configuration
public class SecurityConfig {

    /** BCrypt for all passwords (sign-up and start accounts). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Checked first; only used for URLs under /api/. */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
                .httpBasic(Customizer.withDefaults())
                // No session: every request sends user and password, a login cookie is not used here
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF protects cookie logins; the API has none, so it is off here only
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /** All other URLs: the HTML pages. */
    @Bean
    @Order(2)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/signup", "/actuator/health").permitAll()
                        // /css/**, /js/**, /images/**, favicon from src/main/resources/static
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        // Spring's error page, so 403 and 404 are shown and not turned into a login redirect
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/reception/**").hasRole("RECEPTIONIST")
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(redirectByRole()))
                // POST /logout, then back to /login?logout
                .logout(Customizer.withDefaults());
        return http.build();
    }

    /** After login: each role goes to its own start page. */
    private static AuthenticationSuccessHandler redirectByRole() {
        return (request, response, authentication) ->
                response.sendRedirect(request.getContextPath() + startPage(authentication));
    }

    /** Start page of the logged-in user; also used by GET / in HomeController. */
    public static String startPage(Authentication authentication) {
        // a set, because Spring Security 7 can add more authorities than the role
        Set<String> authorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (authorities.contains("ROLE_ADMIN")) {
            return "/admin";
        }
        if (authorities.contains("ROLE_RECEPTIONIST")) {
            return "/reception";
        }
        return "/customer";
    }
}
