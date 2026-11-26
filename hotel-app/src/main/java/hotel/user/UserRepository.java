package hotel.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for users. Spring Data JPA creates the queries from the method names. */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
