package ucd.comp40660.flight.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ucd.comp40660.flight.exception.FlightNotFoundException;
import ucd.comp40660.flight.model.Flight;
import ucd.comp40660.flight.model.FlightSearch;
import ucd.comp40660.flight.repository.FlightRepository;
import ucd.comp40660.reservation.model.Reservation;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.service.EncryptionService;
import ucd.comp40660.service.UserService;
import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.model.*;
import ucd.comp40660.user.repository.CreditCardRepository;
import ucd.comp40660.user.repository.GuestRepository;
import ucd.comp40660.user.repository.PassengerRepository;
import ucd.comp40660.user.repository.UserRepository;
import ucd.comp40660.validator.CreditCardValidator;
import ucd.comp40660.validator.GuestValidator;
import ucd.comp40660.validator.PassengerValidator;
import ucd.comp40660.validator.UserValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    @Autowired
    CreditCardValidator creditCardValidator;

    @Autowired
    PassengerValidator passengerValidator;

    @Autowired
    GuestValidator guestValidator;


    Long temporaryFlightReference;
    int numberOfPassengers;

    @GetMapping("/home")
    public void home(HttpServletResponse response) throws IOException {
        response.sendRedirect("/");
    }

    //    Get all flights
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/flights")
    public String getAllFlights(HttpServletRequest req, Model model) {
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

        LOGGER.info("Called getAllFlights() by user <" + sessionUser.getUsername() + "> with the role of <" + userRoles + ">");
        List<Flight> flights = flightRepository.findAll();
        model.addAttribute("flights", flights);
        model.addAttribute("sessionUser", sessionUser);


        return "viewAllFlights.html";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/registerFlight")
    public String registerFlight(Model model, HttpServletRequest req) {
        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        return "/createFlight.html";

    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/registerFlight")
    public void registerFlight(Model model, HttpServletRequest req, HttpServletResponse response,
                               String source, String destination, String departureDate, String departureTime,
                               String arrivalDate, String arrivalTime) throws IOException, ParseException {

        LOGGER.info(String.format("VALUES!!!: %s, %s, %s, %s, %s, %s", source, destination, departureDate, departureTime,
                arrivalDate, arrivalTime));


        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        SimpleDateFormat hm = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date dep = df.parse(departureDate + " " + departureTime);
        Date arr = df.parse(arrivalDate + " " + arrivalTime);
        Date dt = hm.parse(departureTime);
        Date at = hm.parse(arrivalTime);
        Date dd = sdf.parse(departureDate);
        Date ad = sdf.parse(arrivalDate);

        LOGGER.info(String.format("Attempting to concatenate dates and times"));
        LOGGER.info(String.format("dep & arr %s, %s", dep.getTime(), arr.getTime()));
        LOGGER.info(String.format("dt & at: %s, %s", dt.getTime(), at.getTime()));
        LOGGER.info(String.format("dd & ad: %s, dt time: %s", dd.getTime(), ad.getTime()));

        Date departure = new Date(dd.getTime() + dt.getTime());
        Date arrival = new Date(ad.getTime() + at.getTime());

        LOGGER.info(String.format("Attempting to create Flight object"));

        Flight flight = new Flight(source, destination, dep, arr);
        flight.setReservations(null);

        List<Flight> flights = flightRepository.findAll();
        model.addAttribute("flights", flights);

        LOGGER.info(String.format("Attempting to save Flight object"));

        flightRepository.saveAndFlush(flight);

        response.sendRedirect("/");
    }

    //    Get a single flight
    @GetMapping("/flights/{id}")
    @ResponseBody
    public Flight getFlightById(@PathVariable(value = "id") Long flightID) throws FlightNotFoundException {

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userSession.getUser().getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Called getFlightById() with id = <" + flightID + "> by user <" + userSession.getUser().getUsername() + "> with the role of <" + userRoles + ">");

        return flightRepository.findById(flightID)
                .orElseThrow(() -> new FlightNotFoundException(flightID));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/updateFlight")
    public String updateFlight(@RequestParam Long flightID, Model model, HttpServletRequest req) {
        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }
        model.addAttribute("FLIGHTID", flightID);

        return "/updateFlight.html";

    }


    //    Update flight details
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/updateFlightInfo")
    public void updateFlightInfo(@RequestParam Long FLIGHTID, String source, String destination, String departureDate, String departureTime,
                                 String arrivalDate, String arrivalTime, HttpServletResponse response, HttpServletRequest req, Model model) throws FlightNotFoundException, ParseException, IOException {
        Flight flight = flightRepository.findById(FLIGHTID)
                .orElseThrow(() -> new FlightNotFoundException(FLIGHTID));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date dep = df.parse(departureDate + " " + departureTime);
        Date arr = df.parse(arrivalDate + " " + arrivalTime);


        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        StringBuilder userRoles = new StringBuilder();
        for (Role role : sessionUser.getRoles()) {
            userRoles.append(role.getName());
        }


        LOGGER.info("Called updateFlight() with id <" + FLIGHTID + "> by user <" + sessionUser.getUsername() + "> with the role of <" + userRoles + ">");


        flight.setSource(source);
        flight.setDestination(destination);
        flight.setArrivalDateTime(arr);
        flight.setDeparture_date_time(dep);

        LOGGER.info("Called updateFlight() with id <" + FLIGHTID);

        flightRepository.saveAndFlush(flight);

        response.sendRedirect("/flights");
    }


    //    Delete a flight record
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "/deleteFlight")
    public void deleteFlight(@RequestParam Long flightID, Model model, HttpServletRequest req, HttpServletResponse response) throws FlightNotFoundException, IOException {
        System.out.println("here:" + flightID);

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        LOGGER.info("Flight ID: " + flightID);

        Flight flight = flightRepository.findById(flightID)
                .orElseThrow(() -> new FlightNotFoundException(flightID));

        flightRepository.delete(flight);

        StringBuilder userRoles = new StringBuilder();
        for (Role role : sessionUser.getRoles()) {
            userRoles.append(role.getName());
        }


        LOGGER.info("Called deleteFlight() with id <" + flightID + "> by user <" + sessionUser.getUsername() + "> with the role of <" + userRoles + ">");

        List<Flight> flights = flightRepository.findAll();
        model.addAttribute("flights", flights);
        model.addAttribute("sessionUser", sessionUser);

        response.sendRedirect("/flights");
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
    @PostMapping("/adminProcessUserFlightSearch")
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

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/adminProcessGuestFlightSearch")
    public void adminProcessGuestFlightSearch(String departure, String destinationInput, int passengers, String outboundDate, String username,
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

        //TODO Better define Guest User object than hardcoded name.
        userSession.setUser(userRepository.findByUsername("testguest"));
        model.addAttribute("user", userRepository.findByUsername("testguest"));

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
        if (isAdmin(sessionUser)) {
            user = userSession.getUser();
        } else {
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
        if (isAdmin(sessionUser)) {
            user = userSession.getUser();
        } else {
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
                model.addAttribute("error", "No flight matching your criteria exist for that date.");
                response.sendRedirect("/");
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

                response.sendRedirect("/displayBookingPage");
            }
        }
    }


    @GetMapping("/displayBookingPage")
    public String guestBooking(Model model, @Valid @ModelAttribute("passengerForm") Passenger passengerForm,
                               @Valid @ModelAttribute("cardForm") CreditCard cardForm, HttpServletRequest req) {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if (isAdmin(sessionUser)) {
            user = userSession.getUser();
        } else {
            user = sessionUser;
        }

        List<CreditCard> creditCards = user.getCredit_cards();

        for (CreditCard card : creditCards) {
            card.setCardholder_name(EncryptionService.decrypt(card.getCardholder_name()));
            card.setCard_number(EncryptionService.decrypt(card.getCard_number()));
            card.setType(EncryptionService.decrypt(card.getType()));
            card.setSecurity_code(EncryptionService.decrypt(card.getSecurity_code()));
        }

        user.setCredit_cards(creditCards);

        model.addAttribute("user", user);

        if (numberOfPassengers > 1) {
            return "passengerDetails.html";
        } else if (isGuest(user)) {
            return "bookingDetails.html";
        } else {
            model.addAttribute("cards", user.getCredit_cards());
            return "displayPaymentPage.html";
        }
    }

    @PostMapping("/processOtherPassengerDetails")
    public String processOtherPassengerDetails(@Valid @ModelAttribute("passengerForm") Passenger passengerForm,
                                               @Valid @ModelAttribute("cardForm") CreditCard cardForm, Model model,
                                               HttpServletResponse response, HttpServletRequest req,
                                               BindingResult bindingResult) throws IOException {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if (isAdmin(sessionUser)) {
            user = userSession.getUser();
        } else {
            user = sessionUser;
        }

        passengerValidator.validate(passengerForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "passengerDetails.html";
        }
        Passenger passenger = new Passenger();

        passenger.setName(passenger.getName());
        passenger.setSurname(passenger.getSurname());
        passenger.setPhone(passenger.getPhone());
        passenger.setAddress(passenger.getAddress());
        passenger.setEmail(passenger.getEmail());

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
        if (numberOfPassengers > 1) {
            return "passengerDetails.html";
        } else if (isGuest(user)) {
            return "bookingDetails.html";
        } else {
            model.addAttribute("cards", user.getCredit_cards());
            return "displayPaymentPage.html";
        }

    }

    @PostMapping("/processGuestPersonalDetails")
    public String processGuestPersonalDetails(@Valid @ModelAttribute("passengerForm") Guest passengerForm, Model model,
                                              @Valid @ModelAttribute("cardForm") CreditCard cardForm,
                                              HttpServletResponse response, HttpServletRequest req, BindingResult bindingResult) {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if (isAdmin(sessionUser)) {
            user = userSession.getUser();
        } else {
            user = sessionUser;
        }
        model.addAttribute("user", user);

        guestValidator.validate(passengerForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "bookingDetails.html";
        }


        guest.setName(passengerForm.getName());
        guest.setSurname(passengerForm.getSurname());
        guest.setEmail(passengerForm.getEmail());
        guest.setPhone(passengerForm.getPhone());
        guest.setAddress(passengerForm.getAddress());

        return "/displayPaymentPage";
    }

    @GetMapping("/displayPaymentPage")
    public String displayPaymentPage(@Valid @ModelAttribute("passengerForm") Guest passengerForm, Model model,
                                     @Valid @ModelAttribute("cardForm") CreditCard cardForm,
                                     HttpServletRequest req) {

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

    @PreAuthorize("hasAuthority('MEMBER') or hasAuthority('ADMIN')")
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
        if (isAdmin(sessionUser)) {
            user = userSession.getUser();
        } else {
            user = sessionUser;
        }
        model.addAttribute("user", user);

        Reservation reservation = new Reservation();
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
            reservation.setEmail(user.getEmail());
            reservationRepository.saveAndFlush(reservation);
            reservation.setCredit_card(card);

            model.addAttribute("user", user);
            model.addAttribute("reservation", reservation);
            model.addAttribute("flight", flight);

            LOGGER.info("User <" + user.getUsername() + "> booked a flight with id <" + flight.getFlightID() + ">");

            return "displayReservation.html";
        }
    }

    @PostMapping("/processGuestPayment")
    public String processGuestPayment(@Valid @ModelAttribute("cardForm") CreditCard cardForm,
                                      @Valid @ModelAttribute("passengerForm") Guest passengerForm, BindingResult bindingResult,
                                      HttpServletResponse response, HttpServletRequest req, Model model) throws IOException {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        User user = null;
        if (isAdmin(sessionUser)) {
            user = userSession.getUser();
        } else {
            user = sessionUser;
        }
        model.addAttribute("user", user);

        creditCardValidator.validate(cardForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "bookingDetails.html";
        }

        Reservation reservation = new Reservation();

        CreditCard card = new CreditCard(EncryptionService.encrypt(cardForm.getCardholder_name()), EncryptionService.encrypt(cardForm.getCard_number()),
                EncryptionService.encrypt(cardForm.getType()), cardForm.getExpiration_month(), cardForm.getExpiration_year(), EncryptionService.encrypt(cardForm.getSecurity_code()));

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

        model.addAttribute("reservation", reservation);
        model.addAttribute("flight", flightRepository.findFlightByFlightID(temporaryFlightReference));
        return "displayReservation.html";

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
        if (isAdmin(sessionUser)) {
            user = userSession.getUser();
        } else {
            user = sessionUser;
        }
        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("user", user);

        List<Reservation> guestReservationId = new ArrayList<>();
        List<Guest> guestList = guestRepository.findAll();
        List<Reservation> reservationList = reservationRepository.findAll();

        for (Reservation reserved : reservationList) {
            for (Guest value : guestList) {
                List<Reservation> reservedLists = value.getReservations();
                for (Reservation reservedList : reservedLists) {
                    if (reservedList.getEmail().equals(reserved.getEmail()) && reservedList.getFlight_reference().equals(reserved.getFlight_reference())) {
                        if (reserved.getFlight_reference().equals(temporaryFlightReference)) {
                            guestReservationId.add(reserved);
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
        if (isAdmin(sessionUser)) {
            user = userSession.getUser();
        } else {
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
            LOGGER.info("Called getGuestReservations(): by guest <" + guest.toString() + ">");
        } else {
            model.addAttribute("error", "Flight is null");
        }

        return "viewFlightsGuest.html";
    }

    private boolean isAdmin(User sessionUser) {
        boolean isAdmin = false;
        Iterator<Role> roleIterator = sessionUser.getRoles().iterator();
        while (roleIterator.hasNext()) {
            if (roleIterator.next().getName().equals("ADMIN")) {
                isAdmin = true;
            }
        }
        return isAdmin;
    }

    private boolean isGuest(User sessionUser) {
        boolean isGuest = false;
        Iterator<Role> roleIterator = sessionUser.getRoles().iterator();
        while (roleIterator.hasNext()) {
            if (roleIterator.next().getName().equals("GUEST")) {
                isGuest = true;
            }
        }
        return isGuest;
    }
}
