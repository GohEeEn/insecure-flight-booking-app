package ucd.comp47660.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp47660.model.Passenger;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
}
