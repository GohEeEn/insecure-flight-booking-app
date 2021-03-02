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
import ucd.comp40660.user.model.Passenger;
import ucd.comp40660.user.repository.GuestRepository;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.PassengerRepository;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Controller
public class FlightController {

    private FlightSearch flightSearch = new FlightSearch();
    private Guest guest = new Guest();

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    GuestRepository guestRepository;

    Long temporaryFlightReference;


    int numberOfPassengers;

    List<Passenger> listOfOtherPassengers = new ArrayList<>();

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
    public void processFlightSearch(String departure, String destinationInput, int passengers, String outboundDate, HttpServletResponse response) throws IOException {

        numberOfPassengers = passengers;

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

        if(numberOfPassengers > 1){
            return "passengerDetails.html";
        }else{
            return "bookingDetails.html";
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

        guest.getPassengers().add(passenger);
        guestRepository.saveAndFlush(guest);

        passengerRepository.saveAndFlush(passenger);
        passenger.setGuest(guest);

        passenger.setGuest(guest);


//        listOfOtherPassengers.add(passenger);



//        passengerRepository.save(passenger);

        System.out.println("size: " + guest.getPassengers().size());

        numberOfPassengers -= 1;
        response.sendRedirect("/displayBookingPage");

//        if(numberOfPassengers > 1){
//            response.sendRedirect("/displayBookingPage");
//        }else{
//            response.sendRedirect("/bookingDetails");
//        }

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
        reservationRepository.save(reservation);

        guestRepository.save(guest);

//        reservationRepository.save(reservation);
//        reservationRepository.saveAndFlush(reservation);

//        re-instantiate the guest to avoid persistence of the same guest
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
