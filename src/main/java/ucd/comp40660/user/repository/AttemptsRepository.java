package ucd.comp40660.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp40660.user.model.Attempts;

import java.util.Optional;

@Repository
public interface AttemptsRepository extends JpaRepository<Attempts, Long> {

    Optional<Attempts> findAttemptsByUsername(String username);
}
