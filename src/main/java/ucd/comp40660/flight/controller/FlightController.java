package ucd.comp40660.flight.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import ucd.comp40660.flight.exception.FlightNotFoundException;
import ucd.comp40660.flight.model.Flight;
import ucd.comp40660.flight.model.FlightSearch;
import ucd.comp40660.flight.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ucd.comp40660.reservation.model.Reservation;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.model.CreditCard;
import ucd.comp40660.user.model.Guest;
import ucd.comp40660.user.model.Passenger;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.CreditCardRepository;
import ucd.comp40660.user.repository.GuestRepository;


import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ucd.comp40660.user.repository.PassengerRepository;

import lombok.extern.log4j.Log4j2;
import ucd.comp40660.user.repository.UserRepository;


@Log4j2
@Controller
public class FlightController {

    private final FlightSearch flightSearch = new FlightSearch();
    private Guest guest = new Guest();

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
        return flightRepository.findAll();
    }

    //    Get a single flight
    @GetMapping("/flights/{id}")
    @ResponseBody
    public Flight getFlightById(@PathVariable(value = "id") Long flightID) throws FlightNotFoundException {
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
        flight.setArrival_date_time(flightDetails.getArrival_date_time());
        flight.setDeparture_date_time(flightDetails.getDeparture_date_time());

        Flight updatedFlight = flightRepository.save(flight);

        return updatedFlight;
    }

    //    Delete a flight record
    @DeleteMapping("/flights/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteFlight(@PathVariable(value = "id") Long flightID) throws FlightNotFoundException {
        Flight flight = flightRepository.findById(flightID)
                .orElseThrow(() -> new FlightNotFoundException(flightID));

        flightRepository.delete(flight);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/processFlightSearch")

    public void processFlightSearch(String departure, String destinationInput, int passengers, String outboundDate,
                                    Model model, HttpServletResponse response) throws IOException {

        numberOfPassengers = passengers;
        flightSearch.setDeparture(departure);
        flightSearch.setDestinationInput(destinationInput);
        flightSearch.setPassengers(passengers);
        flightSearch.setOutboundDate(outboundDate);
        model.addAttribute("user", userSession.getUser());


        response.sendRedirect("/flightSearchResults");
    }

    @GetMapping("/flightSearchResults")
    public String flightSearchResults(Model model) {
        List<Flight> flightList = flightCheck();
//        flightList = flightRepository.findAll();
        model.addAttribute("displayedFlights", flightList);
        model.addAttribute("user", userSession.getUser());

        return "flightResults.html";
    }

    @PostMapping("/selectFlight")
    public void selectFlight(String flightIndexSelected, Model model, HttpServletResponse response) throws IOException {
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
                            && aFlight.getDeparture_date_time().equals(userFlight.getDeparture_date_time()) && aFlight.getArrival_date_time().equals(userFlight.getArrival_date_time())) {
                        temporaryFlightReference = aFlight.getFlightID();
                    }
                }
                model.addAttribute("user", userSession.getUser());

                response.sendRedirect("/displayBookingPage");
            }
        }
    }

    @GetMapping("/displayBookingPage")
    public String guestBooking(Model model){

        model.addAttribute("user", userSession.getUser());

        if(numberOfPassengers > 1){
            return "passengerDetails.html";
        }else if(userSession.getUser()==null){
            return "bookingDetails.html";
        }
        else{
            model.addAttribute("cards", userSession.getUser().getCredit_cards());
            return "displayPaymentPage.html";
        }
    }

    @PostMapping("/processOtherPassengerDetails")
    public void processOtherPassengerDetails(String name, String surname, String email, String phoneNumber, String address, HttpServletResponse response) throws IOException {

        Passenger passenger = new Passenger();

        passenger.setName(name);
        passenger.setSurname(surname);
        passenger.setPhone(phoneNumber);
        passenger.setAddress(address);
        passenger.setEmail(email);

        User user = userSession.getUser();

        if (user == null) {
            guest.getPassengers().add(passenger);
            guestRepository.save(guest);

            passengerRepository.saveAndFlush(passenger);
            passenger.setGuest(guest);

            System.out.println("size: " + guest.getPassengers().size());
        }
        else{
            user.getPassengers().add(passenger);
            userRepository.saveAndFlush(user);

            passengerRepository.saveAndFlush(passenger);
            passenger.setUser(user);
        }

        numberOfPassengers -= 1;
        response.sendRedirect("/displayBookingPage");

    }

    @PostMapping("/processGuestPersonalDetails")
    public String processGuestPersonalDetails(String name, String surname, String email, String phoneNumber, String address,
                                            Model model, HttpServletResponse response ) throws IOException {

        guest.setName(name);
        guest.setSurname(surname);
        guest.setEmail(email);
        guest.setPhone(phoneNumber);
        guest.setAddress(address);

        model.addAttribute("user", userSession.getUser());

        return "/displayPaymentPage";
    }

    @GetMapping("/displayPaymentPage")
    public String displayPaymentPage(Model model){

        if(userSession.getUser()!=null){
            List<CreditCard> cards = creditCardRepository.findAllByUser(userSession.getUser());
            model.addAttribute("cards", cards);
        }

        model.addAttribute("user", userSession.getUser());
        return "displayPaymentPage.html";
    }
  
  @PostMapping("/processMemberPayment")
    public String processMemberPayment(Model model, HttpServletResponse response) throws IOException {
        User user = userSession.getUser();
        Reservation reservation = new Reservation();
        user.getReservations().add(reservation);
        userRepository.flush();
        reservation.setUser(user);

        //TODO Create flight object via tempflightreference and flightrepo
        Flight flight = flightRepository.findFlightByFlightID(temporaryFlightReference);
        reservation.setFlight(flight);
        flight.getReservations().add(reservation);
        flightRepository.saveAndFlush(flight);
        reservationRepository.saveAndFlush(reservation);
        model.addAttribute("user", user);
        model.addAttribute("reservation", reservation);
        model.addAttribute("flight", flight);
        //TODO add Flight object to model for display @ displayReservation.html

        return "displayReservation.html";

    }


    @PostMapping("/processGuestPayment")
    public void processGuestPayment(String cardholder_name, String card_number, String card_type, int expiration_month,
                            int expiration_year, String security_code, Model model,  HttpServletResponse response) throws IOException {

        Reservation reservation = new Reservation();

        // TODO : Set guest's credit card detail required
        CreditCard card = new CreditCard(cardholder_name, card_number, card_type, expiration_month, expiration_year, security_code);
        creditCardRepository.saveAndFlush(card);

        guest.setCredit_card(card);
        guestRepository.save(guest);

        card.setGuest(guest);

        reservation.setEmail(guest.getEmail());

        //TODO Change to Flight Object
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
    public String displayReservationId(Model model) {
        List<Reservation> guestReservationId = new ArrayList<>();
        List<Guest> guestList = guestRepository.findAll();
        System.out.println(guestList.get(1).getPassengers().size() );

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
            String flightStringFormat = availableFlight.getDeparture_date_time().substring(0, 11).trim();
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
    public String getGuestReservations(Model model) {

//        obtain flight and guest objects
        Flight flight = flightRepository.findFlightByFlightID(temporaryFlightReference);
        Guest guest = guestRepository.findTopByOrderByIdDesc();

//        backend log messages
        if (guest != null) {
            log.info(String.format("getGuestReservations(): Guest info: " + guest.toString()));
        } else {
            log.info(String.format("getGuestReservations(): Guest is null"));
        }

        if (flight != null) {
            log.info(String.format("getGuestReservations(): Flight info: " + flight.toString()));
        } else {
            log.info(String.format("getGuestReservations(): Flight is null"));
        }

//        pass flight and guest objects to Thymeleaf frontend
        if (flight != null) {
            model.addAttribute("guest", guest);
            model.addAttribute("flightGuest", flight);
            return "viewFlightsGuest.html";
        } else {
            model.addAttribute("error", "Flight is null");
            return "viewFlightsGuest.html";
        }
    }

}
