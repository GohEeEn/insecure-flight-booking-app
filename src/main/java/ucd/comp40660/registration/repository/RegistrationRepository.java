package ucd.comp40660.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp40660.registration.model.Registration;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
}
