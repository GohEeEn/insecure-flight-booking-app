package ucd.comp40660.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp40660.flight.model.Flight;
import ucd.comp40660.reservation.model.Reservation;
import ucd.comp40660.user.model.Guest;
import ucd.comp40660.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByEmail(String email);
    List<Reservation> findAllByUser(User user);
    Reservation findOneByGuest(Guest guest);
    Reservation findByFlight(Flight flight);
    Reservation findByUserAndFlight(User user, Flight flight);
    List <Reservation> findAllByUserAndCancelledIsTrue(User user);
    List <Reservation> findAllByUserAndCancelledIsFalse(User user);
    Reservation findByUserAndFlightAndCancelledIsTrue(User user, Flight flight);
    Reservation findOneByEmailAndId(String email, Long id);
    Reservation findByGuestAndFlight(Guest guest, Flight flight);

    boolean existsByUserAndFlight(User user, Flight flight);
//    public findFirstByUser(User user);
}
