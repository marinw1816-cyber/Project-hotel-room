package hotel.room;

import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for rooms. Spring Data JPA creates the implementation (findAll, findById, save, ...). */
public interface RoomRepository extends JpaRepository<RoomEntity, Integer> {
}
