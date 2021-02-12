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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FlightController {

    private FlightSearch flightSearch = new FlightSearch();

    @Autowired
    FlightRepository flightRepository;

    @GetMapping("/")
    public String index(){
        return "index.html";
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
    public void processFlightSearch(String departure, String destinationInput, String passengers, String outboundDate, HttpServletResponse response) throws IOException {

        System.out.println(outboundDate);
        flightSearch.setDeparture(departure);
        flightSearch.setDestinationInput(destinationInput);
        flightSearch.setPassengers(passengers);
        flightSearch.setOutboundDate(outboundDate);

        response.sendRedirect("/flightSearchResults");
    }

    @GetMapping("/flightSearchResults")
    public String flightSearchResults(Model model){
        List<Flight> flightList = new ArrayList<>();
        flightList = flightCheck();
//        flightList = flightRepository.findAll();
        model.addAttribute("displayedFlights", flightList);
        return "flightResults.html";
    }

    private List<Flight> flightCheck() {
        List<Flight> availableFlights = new ArrayList<>();
        List<Flight> userFlightOptions = new ArrayList<>();

        availableFlights = flightRepository.findAll();

        int i = 0;
        for( i = 0; i < availableFlights.size(); i++){
            String flightStringFormat = availableFlights.get(i).getDeparture_date_time().toString().substring(0,11).trim();
            if( flightStringFormat.equals(flightSearch.getOutboundDate() ) ){
                if(availableFlights.get(i).getSource().equals(flightSearch.getDeparture() )
                && availableFlights.get(i).getDestination().equals(flightSearch.getDestinationInput()) ){
                    userFlightOptions.add(availableFlights.get(i) );
                }
            }

        }
//        userFlightOptions.add(availableFlights.get(0));
        return userFlightOptions;
    }


}
