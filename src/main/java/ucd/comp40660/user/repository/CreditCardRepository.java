package ucd.comp40660.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ucd.comp40660.user.model.CreditCard;
import ucd.comp40660.user.model.Guest;
import ucd.comp40660.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findAllByUser(User user);

//    public CreditCard findOneByGuest(Guest guest);
}
