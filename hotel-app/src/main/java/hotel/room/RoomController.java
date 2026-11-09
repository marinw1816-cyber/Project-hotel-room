package hotel.room;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** REST API for rooms (admin room CRUD). */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository rooms;

    public RoomController(RoomRepository rooms) {
        this.rooms = rooms;
    }

    /** GET /api/rooms -> 200 with all rooms. */
    @GetMapping
    public List<RoomDto> getAll() {
        return rooms.findAll().stream().map(RoomDto::from).toList();
    }

    /** GET /api/rooms/{roomNumber} -> 200, or 404. */
    @GetMapping("/{roomNumber}")
    public RoomDto getOne(@PathVariable int roomNumber) {
        return RoomDto.from(findOrThrow(roomNumber));
    }

    /** POST /api/rooms -> 201 with Location header, 409 if the number exists, 400 if values are invalid. */
    @PostMapping
    public ResponseEntity<RoomDto> create(@RequestBody RoomDto body) {
        if (body.roomNumber() == null || body.roomNumber() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room number must be positive");
        }
        validate(body);
        if (rooms.existsById(body.roomNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room " + body.roomNumber() + " already exists");
        }
        RoomEntity saved = rooms.save(new RoomEntity(body.roomNumber(), body.capacity(), body.pricePerNight()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{roomNumber}")
                .buildAndExpand(saved.getRoomNumber())
                .toUri();
        return ResponseEntity.created(location).body(RoomDto.from(saved));
    }

    /** PUT /api/rooms/{roomNumber} -> 200, 404, or 400. The room number comes from the URL and is never changed. */
    @PutMapping("/{roomNumber}")
    public RoomDto update(@PathVariable int roomNumber, @RequestBody RoomDto body) {
        RoomEntity room = findOrThrow(roomNumber);
        validate(body);
        room.setCapacity(body.capacity());
        room.setPricePerNight(body.pricePerNight());
        return RoomDto.from(rooms.save(room));
    }

    /** DELETE /api/rooms/{roomNumber} -> 204, or 404. */
    @DeleteMapping("/{roomNumber}")
    public ResponseEntity<Void> delete(@PathVariable int roomNumber) {
        rooms.delete(findOrThrow(roomNumber));
        return ResponseEntity.noContent().build();
    }

    private RoomEntity findOrThrow(int roomNumber) {
        return rooms.findById(roomNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room " + roomNumber + " not found"));
    }

    /** Same rules as hotel.model.Room: capacity > 0, price >= 0. */
    private static void validate(RoomDto body) {
        if (body.capacity() == null || body.capacity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capacity must be positive");
        }
        if (body.pricePerNight() == null || body.pricePerNight().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must not be negative");
        }
    }
}
