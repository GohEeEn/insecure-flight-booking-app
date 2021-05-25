package ucd.comp40660.reservation.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ucd.comp40660.user.model.Guest;
import ucd.comp40660.user.model.Role;
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
import java.util.Optional;

import ucd.comp40660.user.repository.GuestRepository;

import ucd.comp40660.user.repository.UserRepository;
import ucd.comp40660.validator.UserValidator;


@Controller
public class ReservationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationController.class);
    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GuestRepository guestRepository;

    //  use it to implement the get user/member reservations and display it
    @Autowired
    private UserSession userSession;

    @Autowired
    UserValidator userValidator;

    @Autowired
    UserService userService;


    //    Get all reservations
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/reservations")
    public String getAllReservations(HttpServletRequest req, Model model) {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(sessionUser.getUsername()).getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Get list of reservations called by <" +  sessionUser.getUsername() + "> with the role of <" + userRoles + ">");
        List<Reservation> reservations = reservationRepository.findAll();
        model.addAttribute("reservations", reservations);

        return "viewReservations.html";
    }

    //    Get a single reservation by id
    @GetMapping("/reservations/{id}")
    @ResponseBody
    public Reservation getReservationById(@PathVariable(value = "id") Long reservationID) throws ReservationNotFoundException {

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userSession.getUser().getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Get a single reservation by id called by <" + userSession.getUser().getUsername() + "> with the role of <" + userRoles + ">");
        return reservationRepository.findById(reservationID)
                .orElseThrow(() -> new ReservationNotFoundException(reservationID));
    }

    //    Update the details of a reservation record
    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @PutMapping("/reservations/{username}/{id}")
    @ResponseBody
    public Reservation updateReservation(@PathVariable(value = "username") String username, @PathVariable(value = "id") Long reservationID,
                                         @Valid @RequestBody Reservation reservationDetails, HttpServletRequest req) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationID)
                .orElseThrow(() -> new ReservationNotFoundException(reservationID));

        reservation.setEmail(reservationDetails.getEmail());
        reservation.setFlight_reference(reservationDetails.getFlight_reference());

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userSession.getUser().getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Update details of reservation with id = <" + reservationID + ">" + " by user <" + userSession.getUser().getUsername() + "> with the role of <" + userRoles + ">");

        return reservationRepository.saveAndFlush(reservation);
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

        User tempUser = userRepository.findByUsername(username);

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userSession.getUser().getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Delete reservation with id = <" + reservationID + ">" + " by user <" + username + "> with the role of <" + userRoles + ">");

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @GetMapping("/getUserReservations/{username}")
    public String getUserReservations(@PathVariable(value = "username") String username, Model model, HttpServletRequest req) throws ReservationNotFoundException {
        User user = null;
        User sessionUser = null;

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(username).getRoles()) {
            userRoles.append(role.getName());
        }

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
//          model.addAttribute("user", user);
        }

        user = userRepository.findByUsername(username);

        if (user != null) {
//            User user = userSession.getUser();


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
                LOGGER.info("Added flights to front end as 'flightsUser'");
                LOGGER.info("Cancelled Flights: " + cancelled_flights);
                LOGGER.info("getUserReservations() called by <" + username + "> with the role of <" + userRoles + ">");

            } else { // throw an error if there are no reservations
                LOGGER.warn("Unsuccessful attempt to get user reservations by user <" + username + "> with the role of <" + userRoles + ">");
                model.addAttribute("error", "No reservations found");
            }
            model.addAttribute("user", user);
            model.addAttribute("sessionUser", sessionUser);
            return "viewFlightsUser.html";

        } else {
            LOGGER.warn("Unsuccessful attempt to get user reservations by user <" + username + "> with the role of <" + userRoles + ">");
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

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(username).getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Reservation cancelled with flight id = <" + flightID + "> by user <" + username + "> with the role of <" + userRoles + ">");

        response.sendRedirect("/getUserReservations/" + username);
    }

//    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/guestReservations/cancel/{email}/{id}")
    public void cancelGuestReservation(@PathVariable(value = "email") String email, @PathVariable(value = "id") Long flightID,
                                  Model model, HttpServletResponse response, HttpServletRequest req) throws ReservationNotFoundException, IOException {

        LOGGER.info("Beginning Guest cancellation process.");

        Principal userDetails = req.getUserPrincipal();
        User user = userRepository.findByUsername(userDetails.getName());
        User sessionUser = null;


        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
//            LOGGER.info("%s", "List all users called by <" + sessionUser.getUsername() + "> with the role of <" + sessionUser.getRoles() + ">");
        }

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(sessionUser.getUsername()).getRoles()) {
            userRoles.append(role.getName());
        }

        user = sessionUser;
        model.addAttribute("user", user);

//        List<User> users = userRepository.findAll();
//        model.addAttribute("users", users);
        LOGGER.info("Attempting to get Flight Info");

        Flight flight = flightRepository.findFlightByFlightID(flightID);

        LOGGER.info("Attempting to get Guest Info");

        Guest guest = guestRepository.findByEmail(email);
        LOGGER.info(String.format("Flight ID: '%d', Guest: '%s %s %s'", flight.getFlightID(), guest.getName(), guest.getSurname(), guest.getEmail()));


        Reservation reservation = reservationRepository.findByGuestAndFlight(guest, flight);
        reservation.setCancelled(true);
        reservationRepository.saveAndFlush(reservation);
        guestRepository.saveAndFlush(guest);
        flightRepository.saveAndFlush(flight);

        assert sessionUser != null;
        LOGGER.info("Reservation cancelled with flight id = <" + flightID + "> by user <" + sessionUser.getUsername() + "> with the role of <" + userRoles + ">");

        response.sendRedirect("/");


    }




    @PostMapping("/getGuestReservations")
    public String getGuestReservations(Model model, String inputEmail, String inputReservationID, HttpServletRequest req) throws ReservationNotFoundException {

        User user = null;
        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }



//        backend log messages
        if (inputEmail != null && inputReservationID != null) {
            LOGGER.info("Called getGuestReservations(): with email <" + inputEmail + "> and reservation id <" + inputReservationID + ">");
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
//            LOGGER.info(String.format("Called getGuestReservations(): Reservation info: '%s'", reservation));

            // find the flight via the reservation object
            Flight flight = flightRepository.findFlightByReservations(reservation);
            Guest guest = guestRepository.findByEmail(inputEmail);
            LOGGER.info(String.format("Called guest findByEmail from repo, found guest '%s %s'", guest.getName(), guest.getSurname() ));


//            LOGGER.info(String.format("Called getGuestReservations(): Flight info: '%s'", flight));

            // add flight and guest to the model
            model.addAttribute("flightGuest", flight);
            model.addAttribute("guest", guest);
            model.addAttribute("reservation", reservation);


            return "viewFlightsGuest.html";
        } else {

//            if no reservation found with the provided details, keep user on the same page
            model.addAttribute("error", "No such reservation found.");

            return "index.html";
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/updateReservation")
    public String updateReservation(@RequestParam String reservationID, Model model, HttpServletRequest req){
        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }
        model.addAttribute("reservationID", reservationID);


        return "/updateReservation.html";
    }




    @PostMapping("/updateReservationInfo")
    public void updateReservationInfo(@RequestParam Long reservationID, String userDetails,
                                     String flightID, HttpServletResponse response) throws IOException {

        Long flID = Long.parseLong(flightID);

        Optional<Reservation> optionalReservation = reservationRepository.findById(reservationID);
        Flight flight = flightRepository.findFlightByFlightID(flID);

        if(optionalReservation.isPresent()){
            Reservation reservation = optionalReservation.get();

                if(userDetails.contains("@")){
                Guest guest = guestRepository.findByEmail(userDetails);
                reservation.setEmail(guest.getEmail());
                reservation.setGuest(guest);
                reservation.setFlight(flight);
                reservation.setFlight_reference(flight.getFlightID());

                reservationRepository.saveAndFlush(reservation);

                }
                else {
                    User user = userRepository.findByUsername(userDetails);
                    reservation.setEmail(user.getEmail());
                    reservation.setUser(user);
                    reservation.setFlight(flight);
                    reservation.setFlight_reference(flight.getFlightID());

                    reservationRepository.saveAndFlush(reservation);

                }
            }
        else{
            //error
        }

        response.sendRedirect("/reservations");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/deleteReservation")
    public void updateReservationInfo(@RequestParam Long reservationID, HttpServletResponse response)
                                                                            throws ReservationNotFoundException, IOException{

        Reservation reservation = reservationRepository.findById(reservationID)
                .orElseThrow(() -> new ReservationNotFoundException(reservationID));

        reservationRepository.deleteById(reservationID);

        response.sendRedirect("/reservations");
    }


}
