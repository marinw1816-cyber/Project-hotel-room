package hotel.web;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hotel.app.HotelApplication;
import hotel.reservation.ReservationEntity;
import hotel.reservation.ReservationRepository;
import hotel.reservation.ReservationService;
import hotel.reservation.ReservationStatus;
import hotel.room.RoomEntity;
import hotel.room.RoomRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests the customer pages with Spring Security and the H2 test database.
 * Every test runs in a transaction that is rolled back at the end.
 */
@SpringBootTest(classes = HotelApplication.class)
@Transactional
class CustomerControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ReservationService service;

    @Autowired
    private ReservationRepository reservations;

    @Autowired
    private RoomRepository rooms;

    private MockMvc mvc;

    /** Always in the future, so the "not in the past" rule never fails by accident. */
    private final LocalDate day1 = LocalDate.now().plusDays(10);
    private final LocalDate day4 = day1.plusDays(3);

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        reservations.deleteAll();
        rooms.deleteAll();
        rooms.save(new RoomEntity(102, 2, new BigDecimal("90.00")));
    }

    @Test
    @WithMockUser(username = "anna", roles = "CUSTOMER")
    void searchShowsFreeRoomWithTotalPrice() throws Exception {
        mvc.perform(get("/customer/search")
                        .param("checkIn", day1.toString())
                        .param("checkOut", day4.toString())
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("102")))
                .andExpect(content().string(containsString("270.00")))
                .andExpect(content().string(containsString("Book")));
    }

    @Test
    @WithMockUser(username = "anna", roles = "CUSTOMER")
    void searchWithWrongDatesShowsMessage() throws Exception {
        mvc.perform(get("/customer/search")
                        .param("checkIn", day4.toString())
                        .param("checkOut", day1.toString())
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Check-out must be after check-in")));
    }

    @Test
    @WithMockUser(username = "anna", roles = "CUSTOMER")
    void bookingCreatesReservationForLoggedInUser() throws Exception {
        mvc.perform(bookRoom102())
                .andExpect(redirectedUrl("/customer/reservations"))
                .andExpect(flash().attribute("success", containsString("Room 102 booked")));

        List<ReservationEntity> mine = reservations.findByCustomerUsernameOrderByCheckInAsc("anna");
        assertEquals(1, mine.size());
        assertEquals(ReservationStatus.PENDING, mine.get(0).getStatus());

        mvc.perform(get("/customer/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("102")))
                .andExpect(content().string(containsString("PENDING")))
                .andExpect(content().string(containsString("Cancel")));
    }

    @Test
    @WithMockUser(username = "anna", roles = "CUSTOMER")
    void bookingARoomThatIsNotFreeShowsMessage() throws Exception {
        service.book("bob", 102, day1, day4, 2);

        mvc.perform(bookRoom102())
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("not free")));
        assertEquals(0, reservations.findByCustomerUsernameOrderByCheckInAsc("anna").size());
    }

    @Test
    @WithMockUser(username = "anna", roles = "CUSTOMER")
    void bookingWithoutCsrfTokenIsForbidden() throws Exception {
        mvc.perform(post("/customer/book")
                        .param("roomNumber", "102")
                        .param("checkIn", day1.toString())
                        .param("checkOut", day4.toString())
                        .param("guests", "2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "anna", roles = "CUSTOMER")
    void cancelWorks() throws Exception {
        ReservationEntity booked = service.book("anna", 102, day1, day4, 2);

        mvc.perform(post("/customer/reservations/" + booked.getId() + "/cancel").with(csrf()))
                .andExpect(redirectedUrl("/customer/reservations"))
                .andExpect(flash().attribute("success", containsString("cancelled")));
        assertEquals(ReservationStatus.CANCELLED, reservations.findById(booked.getId()).orElseThrow().getStatus());
    }

    @Test
    @WithMockUser(username = "anna", roles = "CUSTOMER")
    void userCannotCancelReservationOfAnotherUser() throws Exception {
        ReservationEntity ofBob = service.book("bob", 102, day1, day4, 2);

        mvc.perform(post("/customer/reservations/" + ofBob.getId() + "/cancel").with(csrf()))
                .andExpect(redirectedUrl("/customer/reservations"))
                .andExpect(flash().attribute("error", containsString("not found")));
        assertEquals(ReservationStatus.PENDING, reservations.findById(ofBob.getId()).orElseThrow().getStatus());
    }

    @Test
    @WithMockUser(username = "anna", roles = "CUSTOMER")
    void changeDatesWorks() throws Exception {
        ReservationEntity booked = service.book("anna", 102, day1, day4, 2);

        mvc.perform(post("/customer/reservations/" + booked.getId() + "/dates").with(csrf())
                        .param("checkIn", day1.toString())
                        .param("checkOut", day4.plusDays(1).toString()))
                .andExpect(redirectedUrl("/customer/reservations"))
                .andExpect(flash().attribute("success", containsString("changed")));
        assertEquals(day4.plusDays(1), reservations.findById(booked.getId()).orElseThrow().getCheckOut());
    }

    @Test
    @WithMockUser(username = "anna", roles = "CUSTOMER")
    void emptyDateShowsMessageNotErrorPage() throws Exception {
        mvc.perform(post("/customer/book").with(csrf())
                        .param("roomNumber", "102")
                        .param("checkIn", "")
                        .param("checkOut", day4.toString())
                        .param("guests", "2"))
                .andExpect(redirectedUrl("/customer/search"))
                .andExpect(flash().attribute("error", containsString("Please fill in")));
    }

    @Test
    @WithMockUser(username = "rita", roles = "RECEPTIONIST")
    void otherRolesGet403() throws Exception {
        mvc.perform(get("/customer/search")).andExpect(status().isForbidden());
    }

    /** POST /customer/book for room 102, day1 to day4, 2 guests, with CSRF token. */
    private org.springframework.test.web.servlet.RequestBuilder bookRoom102() {
        return post("/customer/book").with(csrf())
                .param("roomNumber", "102")
                .param("checkIn", day1.toString())
                .param("checkOut", day4.toString())
                .param("guests", "2");
    }
}
