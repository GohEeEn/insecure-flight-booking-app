package ucd.comp47660.repository;

import org.springframework.transaction.annotation.Transactional;
import ucd.comp47660.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp47660.model.Reservation;

import java.util.Date;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    Flight findFlightByReservations(Reservation reservation);
    Flight findFlightByFlightID(Long flightID);
    List<Flight> findByReservations(Reservation reservation);
    List<Flight> findAllByReservationsAndArrivalDateTimeBefore(Reservation reservation, Date date);
    List<Flight> findAllByReservationsAndArrivalDateTimeAfter(Reservation reservation, Date date);
    List<Flight> findAllByReservationsAndArrivalDateTimeBetween(Reservation reservation, Date start, Date finish);

    @Transactional
    Flight deleteFlightByFlightID(Long id);

}
