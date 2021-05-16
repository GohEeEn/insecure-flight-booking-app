package ucd.comp40660.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ucd.comp40660.user.model.ConfirmationToken;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, String>{
    ConfirmationToken findByConfirmationToken(String confirmationToken);
}
