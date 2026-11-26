package hotel.web;

import hotel.reservation.ReservationEntity;
import hotel.reservation.ReservationService;
import hotel.room.RoomEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pages of the customer: search rooms, book, see, cancel and change the own reservations.
 * Only role CUSTOMER can open /customer/** (see SecurityConfig).
 * Errors of ReservationService are shown as a message on the page, never as an error page.
 */
@Controller
public class CustomerController {

    private final ReservationService reservationService;

    public CustomerController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /** GET /customer/search: the form; with values also the list of free rooms. */
    @GetMapping("/customer/search")
    public String search(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                         @RequestParam(required = false) Integer guests,
                         Model model) {
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("guests", guests);
        if (checkIn == null && checkOut == null && guests == null) {
            return "customer-search";
        }
        try {
            List<RoomEntity> freeRooms = reservationService.findFreeRooms(checkIn, checkOut, guests == null ? 0 : guests);
            model.addAttribute("rooms", freeRooms);
            model.addAttribute("nights", ChronoUnit.DAYS.between(checkIn, checkOut));
        } catch (IllegalArgumentException e) {
            // already on the search page: no redirect needed, the form keeps its values
            model.addAttribute("error", e.getMessage());
        }
        return "customer-search";
    }

    /** POST /customer/book: books the room for the logged-in user. */
    @PostMapping("/customer/book")
    public String book(@RequestParam int roomNumber,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                       @RequestParam int guests,
                       Authentication authentication,
                       RedirectAttributes redirect) {
        try {
            ReservationEntity booked = reservationService.book(authentication.getName(), roomNumber, checkIn, checkOut, guests);
            redirect.addFlashAttribute("success", "Room " + roomNumber + " booked from " + checkIn + " to " + checkOut
                    + ", total price " + booked.totalPrice() + ".");
            return "redirect:/customer/reservations";
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            // back to the search with the same values
            redirect.addAttribute("checkIn", checkIn);
            redirect.addAttribute("checkOut", checkOut);
            redirect.addAttribute("guests", guests);
            return "redirect:/customer/search";
        }
    }

    /** GET /customer/reservations: the reservations of the logged-in user. */
    @GetMapping("/customer/reservations")
    public String reservations(Authentication authentication, Model model) {
        model.addAttribute("reservations", reservationService.myReservations(authentication.getName()));
        return "customer-reservations";
    }

    /** POST /customer/reservations/{id}/cancel: cancels an own reservation. */
    @PostMapping("/customer/reservations/{id}/cancel")
    public String cancel(@PathVariable long id, Authentication authentication, RedirectAttributes redirect) {
        try {
            reservationService.cancel(id, authentication.getName());
            redirect.addFlashAttribute("success", "Reservation " + id + " cancelled.");
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/reservations";
    }

    /** POST /customer/reservations/{id}/dates: changes the dates of an own reservation. */
    @PostMapping("/customer/reservations/{id}/dates")
    public String changeDates(@PathVariable long id,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                              Authentication authentication,
                              RedirectAttributes redirect) {
        try {
            reservationService.changeDates(id, authentication.getName(), checkIn, checkOut);
            redirect.addFlashAttribute("success", "Reservation " + id + " changed to " + checkIn + " - " + checkOut + ".");
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/reservations";
    }

    /** A missing or wrong value in a form (e.g. empty date): message on the page instead of an error page. */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public String badInput(HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "Please fill in all fields with valid values.");
        if (request.getRequestURI().contains("/customer/reservations")) {
            return "redirect:/customer/reservations";
        }
        return "redirect:/customer/search";
    }
}
