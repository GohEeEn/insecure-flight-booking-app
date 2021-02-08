package ucd.comp40660.flight.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RestController;
import ucd.comp40660.flight.exception.FlightNotFoundException;
import ucd.comp40660.flight.model.Flight;
import ucd.comp40660.flight.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
public class FlightController {

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
}
