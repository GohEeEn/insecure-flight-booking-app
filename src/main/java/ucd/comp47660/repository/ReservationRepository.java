package ucd.comp47660.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp47660.model.Flight;
import ucd.comp47660.model.Reservation;
import ucd.comp47660.model.Guest;
import ucd.comp47660.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Reservation findByUserAndFlight(User user, Flight flight);
    List <Reservation> findAllByUserAndCancelledIsTrue(User user);
    List <Reservation> findAllByUserAndCancelledIsFalse(User user);
    Reservation findOneByEmailAndId(String email, Long id);
    Reservation findByGuestAndFlight(Guest guest, Flight flight);
    boolean existsByUserAndFlight(User user, Flight flight);
}
