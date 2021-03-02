package ucd.comp40660.reservation.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import ucd.comp40660.flight.model.Flight;
import ucd.comp40660.flight.repository.FlightRepository;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.reservation.model.Reservation;
import ucd.comp40660.reservation.exception.ReservationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class ReservationController {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    FlightRepository flightRepository;

    //  use it to implement the get user/member reservations and display it
    @Autowired
    private UserSession userSession;

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
    @PutMapping("/reservations/{id}")
    @ResponseBody
    public Reservation updateReservation(@PathVariable(value = "id") Long reservationID, @Valid @RequestBody Reservation reservationDetails) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationID)
                .orElseThrow(() -> new ReservationNotFoundException(reservationID));

        reservation.setEmail(reservationDetails.getEmail());
        reservation.setFlight_reference(reservationDetails.getFlight_reference());

        Reservation reservationUpdated = reservationRepository.save(reservation);

        return reservationUpdated;
    }

    //    Delete a reservation record
    @DeleteMapping("/reservations/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteReservation(@PathVariable(value = "id") Long reservationID) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationID)
                .orElseThrow(() -> new ReservationNotFoundException(reservationID));

        reservationRepository.delete(reservation);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/getUserReservations")
    public String getUserReservations(Model model) throws ReservationNotFoundException {
        if (userSession.getUser() != null) {
            User user = userSession.getUser();

//            backend log messages
            log.info(String.format("UserSession user info: " + user.toString()));

//            add current user to the model
            model.addAttribute("user", user);

//            find all reservations associated with a user
            List<Reservation> reservations = reservationRepository.findAllByUser(user);

            if (reservations.size() > 0) {
//            loop through each reservation and find the flights associated
                List<Flight> flights = new ArrayList<>();

                for (Reservation reservation : reservations) {
                    Flight flight = flightRepository.findFlightByReservations(reservation);

                    if (flight != null) {
                        flights.add(flight);
                    }
                }

//            add the flights to the model, so thymeleaf can display them
                model.addAttribute("flightsUser", flights);
                log.info(String.format("Added flights to front end as \'flightsUser\'"));

            } else { // throw an error if there are no reservations
//                throw new ReservationNotFoundException();
                model.addAttribute("error", "No reservations found");
            }
        }

        return "viewFlightsUser.html";

    }

}
