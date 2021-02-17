package ucd.comp40660.reservation.controller;


import org.springframework.web.bind.annotation.RestController;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.reservation.model.Reservation;
import ucd.comp40660.reservation.exception.ReservationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class ReservationController {

    @Autowired
    ReservationRepository reservationRepository;

    //    Get all reservations
    @GetMapping("/reservations")
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    //    Get a single reservation by id
    @GetMapping("/reservations/{id}")
    public Reservation getReservationById(@PathVariable(value = "id") Long reservationID) throws ReservationNotFoundException {
        return reservationRepository.findById(reservationID)
                .orElseThrow(() -> new ReservationNotFoundException(reservationID));
    }

    //    Update the details of a reservation record
    @PutMapping("/reservations/{id}")
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
    public ResponseEntity<?> deleteReservation(@PathVariable(value = "id") Long reservationID) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationID)
                .orElseThrow(() -> new ReservationNotFoundException(reservationID));

        reservationRepository.delete(reservation);

        return ResponseEntity.ok().build();
    }
}
