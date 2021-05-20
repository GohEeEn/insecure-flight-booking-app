package ucd.comp40660.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp40660.user.model.Guest;

import java.util.Optional;
@Repository
public interface GuestRepository  extends JpaRepository<Guest, Long>{
   Guest findTopByOrderByIdDesc();
   Guest findByEmail(String email);
}
