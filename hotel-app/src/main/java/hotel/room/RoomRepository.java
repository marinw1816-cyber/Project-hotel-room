package hotel.room;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for rooms. Spring Data JPA creates the implementation (findAll, findById, save, ...). */
public interface RoomRepository extends JpaRepository<RoomEntity, Integer> {

    /** Like findById, but with a write lock (SELECT ... FOR UPDATE), used when a room is booked. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomEntity r where r.roomNumber = :roomNumber")
    Optional<RoomEntity> findByIdForUpdate(@Param("roomNumber") Integer roomNumber);
}
