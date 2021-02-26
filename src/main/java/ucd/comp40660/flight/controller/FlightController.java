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
import ucd.comp40660.reservation.exception.ReservationNotFoundException;
import ucd.comp40660.reservation.model.Reservation;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.user.model.Guest;
import ucd.comp40660.user.repository.GuestRepository;


import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.model.User;


@Controller
public class FlightController {


    private FlightSearch flightSearch = new FlightSearch();
    private Guest guest = new Guest();
//    private Reservation reservation = new Reservation();

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    GuestRepository guestRepository;

    Long temporaryFlightReference;

//    @GetMapping("/")
//    public String index(){
//        return "index.html";
//    }


    @PostMapping("/home")
    public void home(String homeButton, HttpServletResponse response) throws IOException {
        response.sendRedirect("/");
    }

    //    Get all flights
    @GetMapping("/flights")
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    //    Get a single flight
    @GetMapping("/flights/{id}")
    public Flight getFlightById(@PathVariable(value = "id") Long flightID) throws FlightNotFoundException {
        return flightRepository.findById(flightID)
                .orElseThrow(() -> new FlightNotFoundException(flightID));
    }

    //    Update flight details
    @PutMapping("/flights/{id}")
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
    public ResponseEntity<?> deleteFlight(@PathVariable(value = "id") Long flightID) throws FlightNotFoundException {
        Flight flight = flightRepository.findById(flightID)
                .orElseThrow(() -> new FlightNotFoundException(flightID));

        flightRepository.delete(flight);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/processFlightSearch")
    public void processFlightSearch(String departure, String destinationInput, int passengers, String outboundDate, HttpServletResponse response) throws IOException {

//        System.out.println(outboundDate);
        flightSearch.setDeparture(departure);
        flightSearch.setDestinationInput(destinationInput);
        flightSearch.setPassengers(passengers);
        flightSearch.setOutboundDate(outboundDate);

        response.sendRedirect("/flightSearchResults");
    }

    @GetMapping("/flightSearchResults")
    public String flightSearchResults(Model model) {
        List<Flight> flightList = new ArrayList<>();
        flightList = flightCheck();
//        flightList = flightRepository.findAll();
        model.addAttribute("displayedFlights", flightList);
        return "flightResults.html";
    }

    @PostMapping("/selectFlight")
    public void selectFlight(String flightIndexSelected, HttpServletResponse response) throws IOException {
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
//                chosen Flight
//                Flight chosenFlight= allFlight.get(flightIndex);
//                temporaryFlightReference = chosenFlight.getFlightID();
                response.sendRedirect("/displayBookingPage");
            }
        }
    }

    @GetMapping("/displayBookingPage")
    public String guestBooking() {

        return "bookingDetails.html";
    }

    @PostMapping("/processGuestPersonalDetails")
    public void processGuestPersonalDetails(String name, String surname, String email, String phoneNumber, String address, HttpServletResponse response) throws IOException {

        guest.setName(name);
        guest.setSurname(surname);
        guest.setEmail(email);
        guest.setPhone(phoneNumber);
        guest.setAddress(address);

        response.sendRedirect("/displayPaymentPage");
    }

    @GetMapping("/displayPaymentPage")
    public String displayPaymentPage() {

        return "displayPaymentPage.html";
    }

    @PostMapping("/processPayment")
    public void processPayment(String credit_card_details, HttpServletResponse response) throws IOException {

        Reservation reservation = new Reservation();

        guest.setCredit_card_details(credit_card_details);

        reservation.setEmail(guest.getEmail());
        reservation.setFlight_reference(temporaryFlightReference);
        reservation.setGuest(guest);

        guest.getReservations().add(reservation);
        guestRepository.save(guest);

        reservationRepository.save(reservation);

        response.sendRedirect("/displayReservationId");
    }

    @GetMapping("/displayReservationId")
    public String displayReservationId(Model model) {
        List<Reservation> guestReservationId = new ArrayList<>();
        List<Guest> guestList = guestRepository.findAll();
        List<Reservation> reservationList = reservationRepository.findAll();

        for (Reservation reserved : reservationList) {
            List<Reservation> reservedLists = new ArrayList<>();
            for (int i = 0; i < guestList.size(); i++) {
                reservedLists = guestList.get(i).getReservations();
                for (Reservation reservedList : reservedLists) {
                    if (reservedList.getEmail().equals(reserved.getEmail()) && reservedList.getFlight_reference().equals(reserved.getFlight_reference())) {
//                        String flightRef = Long.toString(temporaryFlightReference);
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
        List<Flight> availableFlights = new ArrayList<>();
        List<Flight> userFlightOptions = new ArrayList<>();

        availableFlights = flightRepository.findAll();

        int i = 0;
        for (i = 0; i < availableFlights.size(); i++) {
            String flightStringFormat = availableFlights.get(i).getDeparture_date_time().toString().substring(0, 11).trim();
            if (flightStringFormat.equals(flightSearch.getOutboundDate())) {
                if (availableFlights.get(i).getSource().equals(flightSearch.getDeparture())
                        && availableFlights.get(i).getDestination().equals(flightSearch.getDestinationInput())) {
                    userFlightOptions.add(availableFlights.get(i));
                }
            }

        }
//        userFlightOptions.add(availableFlights.get(0));
        return userFlightOptions;
    }

    @GetMapping("/getGuestReservations")
    public String getGuestReservations(Model model) throws ReservationNotFoundException {
        if (guest != null) {

//            add guest to the model
            model.addAttribute("guest", guest);

//            retrieve all reservations and flights
            List<Reservation> reservations = reservationRepository.findAllByGuest(guest);

            if (reservations.size() > 0) {

                List<Flight> flights = new ArrayList<>();

                for (Reservation reservation : reservations) {
                    Flight flight = flightRepository.findFlightByReservation(reservation);

                    if (flight != null) {
                        flights.add(flight);
                    }
                }

//            add all flights corresponding to the user to the model
                model.addAttribute("flightsGuest", flights);

//                display the reservations on a new page
                return "viewFlightsGuest";

            } else {
                throw new ReservationNotFoundException();
            }
        }
//        if guest doesn't have any reservations - return to landing page
        return "/";
    }

}
