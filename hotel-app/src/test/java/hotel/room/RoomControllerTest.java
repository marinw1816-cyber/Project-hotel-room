package hotel.room;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import hotel.app.HotelApplication;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Tests the REST API with the H2 test database (no MariaDB needed). */
@SpringBootTest(classes = HotelApplication.class)
class RoomControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RoomRepository rooms;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        rooms.deleteAll();
    }

    private static String json(int roomNumber, int capacity, String price) {
        return "{\"roomNumber\": " + roomNumber + ", \"capacity\": " + capacity + ", \"pricePerNight\": " + price + "}";
    }

    private void createRoom101() throws Exception {
        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(json(101, 2, "80.00")))
                .andExpect(status().isCreated());
    }

    @Test
    void createReturns201WithLocationAndStoresTheRoom() throws Exception {
        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(json(101, 2, "80.00")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/rooms/101")))
                .andExpect(jsonPath("$.roomNumber").value(101))
                .andExpect(jsonPath("$.capacity").value(2));

        RoomEntity stored = rooms.findById(101).orElseThrow();
        assertEquals(2, stored.getCapacity());
        assertEquals(0, new BigDecimal("80.00").compareTo(stored.getPricePerNight()));
    }

    @Test
    void createExistingRoomNumberReturns409() throws Exception {
        createRoom101();
        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(json(101, 4, "120.00")))
                .andExpect(status().isConflict());
        assertEquals(2, rooms.findById(101).orElseThrow().getCapacity());
    }

    @Test
    void createWithInvalidValuesReturns400() throws Exception {
        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(json(101, 0, "80.00")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(json(101, 2, "-1.00")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(json(0, 2, "80.00")))
                .andExpect(status().isBadRequest());
        assertEquals(0, rooms.count());
    }

    @Test
    void getAllReturnsTheList() throws Exception {
        createRoom101();
        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(json(102, 4, "120.00")))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOneReturns200Or404() throws Exception {
        createRoom101();
        mvc.perform(get("/api/rooms/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value(101))
                .andExpect(jsonPath("$.capacity").value(2));

        mvc.perform(get("/api/rooms/999")).andExpect(status().isNotFound());
    }

    @Test
    void updateChangesValuesButNotTheRoomNumber() throws Exception {
        createRoom101();
        mvc.perform(put("/api/rooms/101").contentType(MediaType.APPLICATION_JSON).content(json(555, 3, "95.50")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value(101))
                .andExpect(jsonPath("$.capacity").value(3));

        RoomEntity stored = rooms.findById(101).orElseThrow();
        assertEquals(3, stored.getCapacity());
        assertEquals(0, new BigDecimal("95.50").compareTo(stored.getPricePerNight()));
        assertFalse(rooms.existsById(555));
    }

    @Test
    void updateReturns404Or400() throws Exception {
        mvc.perform(put("/api/rooms/999").contentType(MediaType.APPLICATION_JSON).content(json(999, 2, "80.00")))
                .andExpect(status().isNotFound());

        createRoom101();
        mvc.perform(put("/api/rooms/101").contentType(MediaType.APPLICATION_JSON).content(json(101, -1, "80.00")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/rooms/101").contentType(MediaType.APPLICATION_JSON).content(json(101, 2, "-5")))
                .andExpect(status().isBadRequest());
        assertEquals(2, rooms.findById(101).orElseThrow().getCapacity());
    }

    @Test
    void deleteReturns204ThenTheRoomIsGone() throws Exception {
        createRoom101();
        mvc.perform(delete("/api/rooms/101")).andExpect(status().isNoContent());
        assertFalse(rooms.existsById(101));

        mvc.perform(delete("/api/rooms/101")).andExpect(status().isNotFound());
    }
}
