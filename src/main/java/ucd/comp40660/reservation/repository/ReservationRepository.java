package ucd.comp40660.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ucd.comp40660.reservation.model.Reservation;
import org.springframework.stereotype.Repository;
import ucd.comp40660.user.model.Guest;
import ucd.comp40660.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByEmail(String email);
    public List<Reservation> findAllByUser(User user);
}
