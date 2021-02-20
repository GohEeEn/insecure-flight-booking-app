package ucd.comp40660.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ucd.comp40660.reservation.model.Reservation;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByEmail(String email);
}
