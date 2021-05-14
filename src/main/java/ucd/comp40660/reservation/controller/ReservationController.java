package ucd.comp40660.reservation.controller;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ucd.comp40660.flight.model.Flight;
import ucd.comp40660.flight.repository.FlightRepository;
import ucd.comp40660.reservation.exception.ReservationNotFoundException;
import ucd.comp40660.reservation.model.Reservation;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.service.UserService;
import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import ucd.comp40660.user.repository.UserRepository;
import ucd.comp40660.validator.UserValidator;


@Log4j2
@Controller
public class ReservationController {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    UserRepository userRepository;

    //  use it to implement the get user/member reservations and display it
    @Autowired
    private UserSession userSession;

    @Autowired
    UserValidator userValidator;

    @Autowired
    UserService userService;


    //    Get all reservations
    @GetMapping("/reservations")
    @ResponseBody
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    //    Get a single reservation by id
    @GetMapping("/reservations/{id}")
    @ResponseBody
    public Reservation getReservationById(@PathVariable(value = "id") Long reservationID) throws ReservationNotFoundException {
        return reservationRepository.findById(reservationID)
                .orElseThrow(() -> new ReservationNotFoundException(reservationID));
    }

    //    Update the details of a reservation record
    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @PutMapping("/reservations/{username}/{id}")
    @ResponseBody
    public Reservation updateReservation(@PathVariable(value = "username") String username, @PathVariable(value = "id") Long reservationID,
                                         @Valid @RequestBody Reservation reservationDetails) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationID)
                .orElseThrow(() -> new ReservationNotFoundException(reservationID));

        reservation.setEmail(reservationDetails.getEmail());
        reservation.setFlight_reference(reservationDetails.getFlight_reference());

        return reservationRepository.save(reservation);
    }

    //    Delete a reservation record
    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @DeleteMapping("/reservations/{username}/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteReservation(@PathVariable(value = "username") String username, @PathVariable(value = "id") Long reservationID)
            throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationID)
                .orElseThrow(() -> new ReservationNotFoundException(reservationID));

        reservationRepository.delete(reservation);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @GetMapping("/getUserReservations/{username}")
    public String getUserReservations(@PathVariable(value = "username") String username, Model model, HttpServletRequest req) throws ReservationNotFoundException {
        User user = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            user = userRepository.findByUsername(username);
//            model.addAttribute("user", user);
        }

        if (user != null) {
//            User user = userSession.getUser();

            // backend log messages
            log.info(String.format("UserSession user info: " + user.toString() + "\n"));


            // add current user to the model
//            model.addAttribute("user", user);


            // find all reservations associated with a user
            List<Reservation> reservations = reservationRepository.findAllByUserAndCancelledIsFalse(user);
            List<Reservation> cancelled_reservations = reservationRepository.findAllByUserAndCancelledIsTrue(user);


            if (reservations.size() > 0 || cancelled_reservations.size() > 0) {
//            loop through each reservation and find the flights associated

                List<Flight> flights = new ArrayList<>();
                List<Flight> upcoming = new ArrayList<>();
                List<Flight> past = new ArrayList<>();
                List<Flight> upcoming_cancellable = new ArrayList<>();
                List<Flight> cancelled_flights = new ArrayList<>();

                Date date = new Date();
                Timestamp now = new Timestamp(date.getTime());
                Timestamp cancellable = new Timestamp(date.getTime() + (3600 * 1000 * 24));

                for (Reservation reservation : reservations) {
                    Flight flight = flightRepository.findFlightByReservations(reservation);
                    upcoming_cancellable.addAll(flightRepository.findAllByReservationsAndArrivalDateTimeAfter(reservation, cancellable));
                    upcoming.addAll(flightRepository.findAllByReservationsAndArrivalDateTimeBetween(reservation, now, cancellable));
                    past.addAll(flightRepository.findAllByReservationsAndArrivalDateTimeBefore(reservation, now));

                    if (flight != null) {
                        flights.add(flight);
                    }
                }

                for (Reservation cancelled_reservation : cancelled_reservations) {
                    cancelled_flights.add(flightRepository.findFlightByReservations(cancelled_reservation));
                }

                // add the flights to the model, so thymeleaf can display them
                model.addAttribute("flightsUser", flights);
                model.addAttribute("upcoming", upcoming);
                model.addAttribute("past", past);
                model.addAttribute("cancelled_flights", cancelled_flights);
                model.addAttribute("upcoming_cancellable", upcoming_cancellable);
                log.info("Added flights to front end as 'flightsUser'");
                log.info("Cancelled Flights: " + cancelled_flights);

            } else { // throw an error if there are no reservations
                model.addAttribute("error", "No reservations found");
            }
            model.addAttribute("user", userRepository.findByUsername(username));
            return "viewFlightsUser.html";

        }
        else{
            model.addAttribute("error", "No Member logged in");
            return "index.html";
        }


    }

    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @GetMapping("/reservations/cancel/{username}/{id}")
    public void cancelReservation(@PathVariable(value = "username") String username, @PathVariable(value = "id") Long flightID,
                                  Model model, HttpServletResponse response, HttpServletRequest req) throws ReservationNotFoundException, IOException {
        User user = null;

        Principal userDetails = req.getUserPrincipal();
//        if (userDetails != null) {
//            user = userRepository.findByUsername(username);
//            model.addAttribute("user", user);
//        }
        user = userRepository.findByUsername(username);

        Flight flight = flightRepository.findFlightByFlightID(flightID);

//        User user = userSession.getUser();
        Reservation reservation = reservationRepository.findByUserAndFlight(user, flight);
        reservation.setCancelled(true);
        reservationRepository.saveAndFlush(reservation);
        userRepository.saveAndFlush(user);
        flightRepository.saveAndFlush(flight);
        model.addAttribute("user", userRepository.findByUsername(userDetails.getName()));
        response.sendRedirect("/getUserReservations/" + username);
    }

    @PostMapping("/getGuestReservations")
    public String getGuestReservations(Model model, String inputEmail, String inputReservationID) throws ReservationNotFoundException {

//        backend log messages
        if (inputEmail != null && inputReservationID != null) {
            log.info("getGuestReservations(): Email: " + inputEmail);
            log.info("getGuestReservations(): Reservation ID: " + inputReservationID);
        }

        Long id;

        try {
            id = Long.parseLong(inputReservationID);
        } catch (NumberFormatException e) {
            return "index.html";
        }

        // find the reservation via email and reservation id
        Reservation reservation = reservationRepository.findOneByEmailAndId(inputEmail, id);

        if (reservation != null) {
            log.info(String.format("getGuestReservations(): Reservation info: '%s'", reservation));

            // find the flight via the reservation object
            Flight flight = flightRepository.findFlightByReservations(reservation);

            log.info(String.format("getGuestReservations(): Flight info: '%s'", flight));

            // add flight to the model
            model.addAttribute("flightGuest", flight);

            return "viewFlightsGuest.html";
        } else {

//            if no reservation found with the provided details, keep user on the same page
            model.addAttribute("error", "No such reservation found.");
          
            return "index.html";
        }
    }

}
