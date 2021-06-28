package ucd.comp47660.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp47660.model.Guest;

@Repository
public interface GuestRepository  extends JpaRepository<Guest, Long>{
   Guest findTopByOrderByIdDesc();
   Guest findByEmail(String email);
}
