package ucd.comp40660.flight.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ucd.comp40660.flight.exception.FlightNotFoundException;
import ucd.comp40660.flight.model.Flight;
import ucd.comp40660.flight.model.FlightSearch;
import ucd.comp40660.flight.repository.FlightRepository;
import ucd.comp40660.reservation.model.Reservation;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.service.UserService;
import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.model.*;
import ucd.comp40660.user.repository.CreditCardRepository;
import ucd.comp40660.user.repository.GuestRepository;
import ucd.comp40660.user.repository.PassengerRepository;
import ucd.comp40660.user.repository.UserRepository;
import ucd.comp40660.validator.UserValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Controller
public class FlightController {

    private final FlightSearch flightSearch = new FlightSearch();
    private Guest guest = new Guest();
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightController.class);

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    GuestRepository guestRepository;

    @Autowired
    CreditCardRepository creditCardRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserSession userSession;

    @Autowired
    UserValidator userValidator;

    @Autowired
    UserService userService;



    Long temporaryFlightReference;
    int numberOfPassengers;

    @PostMapping("/home")
    public void home(HttpServletResponse response) throws IOException {
        response.sendRedirect("/");
    }

    //    Get all flights
    @GetMapping("/flights")
    @ResponseBody
    public List<Flight> getAllFlights() {
        LOGGER.info("%s", "Called getAllFlights() by user <" + userSession.getUser().getUsername() + "> with the role of <" + userSession.getUser().getRoles() + ">");
        return flightRepository.findAll();
    }

    //    Get a single flight
    @GetMapping("/flights/{id}")
    @ResponseBody
    public Flight getFlightById(@PathVariable(value = "id") Long flightID) throws FlightNotFoundException {

        LOGGER.info("%s", "Called getFlightById() with id = <" + flightID + "> by user <" + userSession.getUser().getUsername() + "> with the role of <" + userSession.getUser().getRoles() + ">");

        return flightRepository.findById(flightID)
                .orElseThrow(() -> new FlightNotFoundException(flightID));
    }

    //    Update flight details
    @PutMapping("/flights/{id}")
    @ResponseBody
    public Flight updateFlight(@PathVariable(value = "id") Long flightID, @Valid @RequestBody Flight flightDetails) throws FlightNotFoundException {
        Flight flight = flightRepository.findById(flightID)
                .orElseThrow(() -> new FlightNotFoundException(flightID));

        flight.setSource(flightDetails.getSource());
        flight.setDestination(flightDetails.getDestination());
        flight.setArrivalDateTime(flightDetails.getArrivalDateTime());
        flight.setDeparture_date_time(flightDetails.getDeparture_date_time());

        LOGGER.info("%s", "Called updateFlight() with id <" + flightID + "> by user <" + userSession.getUser().getUsername() + "> with the role of <" + userSession.getUser().getRoles() + ">");

        return flightRepository.save(flight);
    }

    //    Delete a flight record
    @DeleteMapping("/flights/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteFlight(@PathVariable(value = "id") Long flightID) throws FlightNotFoundException {
        Flight flight = flightRepository.findById(flightID)
                .orElseThrow(() -> new FlightNotFoundException(flightID));

        flightRepository.delete(flight);

        LOGGER.info("%s", "Called deleteFlight() with id <" + flightID + "> by user <" + userSession.getUser().getUsername() + "> with the role of <" + userSession.getUser().getRoles() + ">");

        return ResponseEntity.ok().build();
    }

    @PostMapping("/processFlightSearch")

    public void processFlightSearch(String departure, String destinationInput, int passengers, String outboundDate,
                                    Model model, HttpServletResponse response, HttpServletRequest req) throws IOException {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }


        numberOfPassengers = passengers;
        flightSearch.setDeparture(departure);
        flightSearch.setDestinationInput(destinationInput);
        flightSearch.setPassengers(passengers);
        flightSearch.setOutboundDate(outboundDate);
        model.addAttribute("user", sessionUser);

        response.sendRedirect("/flightSearchResults");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/adminProcessFlightSearch")
    public void adminProcessFlightSearch(String departure, String destinationInput, int passengers, String outboundDate, String username,
                                    Model model, HttpServletResponse response, HttpServletRequest req) throws IOException {
        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }


        numberOfPassengers = passengers;
        flightSearch.setDeparture(departure);
        flightSearch.setDestinationInput(destinationInput);
        flightSearch.setPassengers(passengers);
        flightSearch.setOutboundDate(outboundDate);

        userSession.setUser(userRepository.findByUsername(username));
        model.addAttribute("user", userRepository.findByUsername(username));

        response.sendRedirect("/flightSearchResults");
    }


    @GetMapping("/flightSearchResults")
    public String flightSearchResults(Model model, HttpServletRequest req) {
        List<Flight> flightList = flightCheck();

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }


        model.addAttribute("displayedFlights", flightList);

        //Determine if booking as a Member/Guest, or as an admin.
        User user = null;
        if(isAdmin(sessionUser)){
            user = userSession.getUser();
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);

        return "flightResults.html";
    }


    @PostMapping("/selectFlight")
    public void selectFlight(String flightIndexSelected, Model model, HttpServletResponse response, HttpServletRequest req) throws IOException {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if(isAdmin(sessionUser)){
            user = userSession.getUser();
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);


        boolean isNumber = flightIndexSelected.chars().allMatch(Character::isDigit);
        if (!isNumber) {
            PrintWriter out = response.getWriter();
            out.println("<script>");
            out.println("alert('" + "Select from displayed Index" + "');");
            out.println("window.location.replace('" + "/flightSearchResults" + "');");
            out.println("</script>");
        } else {
            List<Flight> flightList = flightCheck();
            int flightIndex = Integer.parseInt(flightIndexSelected);
            if (flightIndex <= 0 || flightIndex > flightList.size()) {
                PrintWriter out = response.getWriter();
                out.println("<script>");
                out.println("alert('" + "Select from displayed Index" + "');");
                out.println("window.location.replace('" + "/flightSearchResults" + "');");
                out.println("</script>");
            } else {
                List<Flight> allFlight = flightRepository.findAll();
                List<Flight> flightOptions = flightCheck();
                Flight userFlight = flightOptions.get(flightIndex - 1);
                for (Flight aFlight : allFlight) {
                    if (aFlight.getDestination().equals(userFlight.getDestination()) && aFlight.getSource().equals(userFlight.getSource())
                            && aFlight.getDeparture_date_time().equals(userFlight.getDeparture_date_time()) && aFlight.getArrivalDateTime().equals(userFlight.getArrivalDateTime())) {
                        temporaryFlightReference = aFlight.getFlightID();
                    }
                }
//                model.addAttribute("user", user);

                response.sendRedirect("/displayBookingPage");
            }
        }
    }



    @GetMapping("/displayBookingPage")
    public String guestBooking(Model model, HttpServletRequest req) {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if(isAdmin(sessionUser)){
            user = userSession.getUser();
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);


//        model.addAttribute("user", userSession.getUser());


        if (numberOfPassengers > 1) {
            return "passengerDetails.html";
        } else if (isGuest(sessionUser)) {
            return "bookingDetails.html";
        } else {
            model.addAttribute("cards", user.getCredit_cards());
            return "displayPaymentPage.html";
        }
    }

    @PostMapping("/processOtherPassengerDetails")
    public void processOtherPassengerDetails(String name, String surname, String email, String phoneNumber, String address,
                                             HttpServletResponse response, HttpServletRequest req) throws IOException {

        Passenger passenger = new Passenger();

        passenger.setName(name);
        passenger.setSurname(surname);
        passenger.setPhone(phoneNumber);
        passenger.setAddress(address);
        passenger.setEmail(email);

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
//            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if(isAdmin(sessionUser)){
            user = userSession.getUser();
        }
        else{
            user = sessionUser;
        }


//        User user = userSession.getUser();

        if (user == null) {
            guest.getPassengers().add(passenger);
            guestRepository.save(guest);

            passengerRepository.saveAndFlush(passenger);
            passenger.setGuest(guest);

        } else {
            user.getPassengers().add(passenger);
            userRepository.saveAndFlush(user);

            passengerRepository.saveAndFlush(passenger);
            passenger.setUser(user);
        }

        numberOfPassengers -= 1;
        response.sendRedirect("/displayBookingPage");

    }

    @PostMapping("/processGuestPersonalDetails")
    public String processGuestPersonalDetails(String name, String surname, String email, String phoneNumber,
                                              String address, Model model, HttpServletRequest req) {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if(isAdmin(sessionUser)){
            user = userSession.getUser();
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);


        guest.setName(name);
        guest.setSurname(surname);
        guest.setEmail(email);
        guest.setPhone(phoneNumber);
        guest.setAddress(address);

        model.addAttribute("user", userSession.getUser());

        return "/displayPaymentPage";
    }

    @GetMapping("/displayPaymentPage")
    public String displayPaymentPage(Model model, HttpServletRequest req) {

        User user = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            user = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("user", user);
        }

        if (user != null) {
            List<CreditCard> cards = creditCardRepository.findAllByUser(user);
            model.addAttribute("cards", cards);
        }

        model.addAttribute("user", user);
        return "displayPaymentPage.html";
    }

    @PostMapping("/processMemberPayment")
    public String processMemberPayment(Model model, CreditCard card, HttpServletRequest req) {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        //Determine if booking as admin
        User user = null;
        if(isAdmin(sessionUser)){
            user = userSession.getUser();
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);

//      User user = userSession.getUser();
      Reservation reservation = new Reservation();
//        if(!user.getReservations().contains())
      user.getReservations().add(reservation);
      userRepository.flush();
      reservation.setUser(user);


      Flight flight = flightRepository.findFlightByFlightID(temporaryFlightReference);
      if (reservationRepository.existsByUserAndFlight(user, flight)) {
          model.addAttribute("user", user);
          model.addAttribute("error", "Flight already booked by Member, new booking cancelled.\n");
          return "index.html";
      } else {
          reservation.setFlight(flight);
          flight.getReservations().add(reservation);
          flightRepository.saveAndFlush(flight);
          reservationRepository.saveAndFlush(reservation);
          reservation.setCredit_card(card);

          model.addAttribute("user", user);
          model.addAttribute("reservation", reservation);
          model.addAttribute("flight", flight);

          return "displayReservation.html";

      }
  }



    @PostMapping("/processGuestPayment")
    public void processGuestPayment(String cardholder_name, String card_number, String card_type, int expiration_month,
                                    int expiration_year, String security_code,
                                    HttpServletResponse response, HttpServletRequest req, Model model) throws IOException {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if(isAdmin(sessionUser)){
            user = userSession.getUser();
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);

        Reservation reservation = new Reservation();

        CreditCard card = new CreditCard(cardholder_name, card_number, card_type, expiration_month, expiration_year, security_code);
        creditCardRepository.saveAndFlush(card);

        guestRepository.save(guest);
        reservation.setCredit_card(card);

        card.getReservations().add(reservation);

        reservation.setEmail(guest.getEmail());
        reservation.setFlight_reference(temporaryFlightReference);//Should be made redundant with Flight object now used
        reservation.setFlight(flightRepository.findFlightByFlightID(temporaryFlightReference));

        reservationRepository.saveAndFlush(reservation);
        reservation.setGuest(guest);

        guest.getReservations().add(reservation);

        guestRepository.save(guest);
        guest = new Guest();

        response.sendRedirect("/displayReservationId");
    }

    @GetMapping("/displayReservationId")
    public String displayReservationId(Model model, HttpServletRequest req) {
        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if(isAdmin(sessionUser)){
            user = userSession.getUser();
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);

        List<Reservation> guestReservationId = new ArrayList<>();
        List<Guest> allGuests = guestRepository.findAll();
//        log.info(allGuests.get(1).getPassengers().size());

        List<Reservation> allReservations = reservationRepository.findAll();

        for (Reservation reservation : allReservations) {
            for (Guest guest : allGuests) {
                List<Reservation> guestReservations = guest.getReservations();
                for (Reservation guestReservation : guestReservations) {
                    if (guestReservation.getEmail().equals(reservation.getEmail()) && guestReservation.getFlight_reference().equals(reservation.getFlight_reference())) {
                        if (reservation.getFlight_reference().equals(temporaryFlightReference)) {
                            guestReservationId.add(reservation);
                        }
                    }
                }
            }
        }
        model.addAttribute("guestReservationIds", guestReservationId);
        return "displayReservation.html";
    }

    private List<Flight> flightCheck() {

        List<Flight> userFlightOptions = new ArrayList<>();
        List<Flight> availableFlights = flightRepository.findAll();

        for (Flight availableFlight : availableFlights) {
            String flightStringFormat = availableFlight.getDeparture_date_time().toString().substring(0, 11).trim();
            if (flightStringFormat.equals(flightSearch.getOutboundDate())) {
                if (availableFlight.getSource().equals(flightSearch.getDeparture())
                        && availableFlight.getDestination().equals(flightSearch.getDestinationInput())) {
                    userFlightOptions.add(availableFlight);
                }
            }

        }

        return userFlightOptions;
    }

    @GetMapping("/getGuestReservations")
    public String getGuestReservations(Model model, HttpServletRequest req) {
        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if(isAdmin(sessionUser)){
            user = userSession.getUser();
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);


        Flight flight = flightRepository.findFlightByFlightID(temporaryFlightReference);
        Guest guest = guestRepository.findTopByOrderByIdDesc();

        if (guest != null)
            LOGGER.info("getGuestReservations(): Guest info: " + guest);
        else
            LOGGER.info("getGuestReservations(): Guest is null");

        if (flight != null)
            LOGGER.info("getGuestReservations(): Flight info: " + flight);
        else
            LOGGER.info("getGuestReservations(): Flight is null");

        if (flight != null) {
            model.addAttribute("guest", guest);
            model.addAttribute("flightGuest", flight);
            LOGGER.info("%s", "Called getGuestReservations(): by guest <" + guest + ">");
        } else {
            model.addAttribute("error", "Flight is null");
        }

        return "viewFlightsGuest.html";
    }

    private boolean isAdmin(User sessionUser) {
        boolean isAdmin = false;
        Iterator<Role> roleIterator = sessionUser.getRoles().iterator();
        while(roleIterator.hasNext()){
            if(roleIterator.next().getName().equals("ADMIN")){
                isAdmin = true;
            }
        }
        return isAdmin;
    }

    private boolean isGuest(User sessionUser) {
        boolean isGuest = false;
        Iterator<Role> roleIterator = sessionUser.getRoles().iterator();
        while(roleIterator.hasNext()){
            if(roleIterator.next().getName().equals("GUEST")){
                isGuest = true;
            }
        }
        return isGuest;
    }


}
