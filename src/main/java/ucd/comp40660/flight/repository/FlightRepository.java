package ucd.comp40660.flight.repository;

import ucd.comp40660.flight.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp40660.reservation.model.Reservation;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    Flight findFlightByReservations(Reservation reservation);
    Flight findFlightByFlightID(Long flightID);
}
