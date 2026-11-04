package hotel.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Spring Boot entry point; scans all packages under "hotel", not only hotel.app
@SpringBootApplication(scanBasePackages = "hotel")
// JPA entities and repositories may live in any package under "hotel"
@EntityScan("hotel")
@EnableJpaRepositories("hotel")
public class HotelApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelApplication.class, args);
    }
}
