package ucd.comp40660.user.controller;
import ucd.comp40660.user.UserSession;
import org.springframework.stereotype.Controller;
import ucd.comp40660.user.exception.CreditCardNotFoundException;
import ucd.comp40660.user.exception.UserNotFoundException;
import ucd.comp40660.user.model.CreditCard;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.CreditCardRepository;
import ucd.comp40660.user.repository.GuestRepository;
import ucd.comp40660.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;


import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;


@Controller
public class CardController {

    @Autowired
    private UserSession userSession;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GuestRepository guestRepository;

    @Autowired
    CreditCardRepository creditCardRepository;

//    @GetMapping("/cards")
//    @ResponseBody
//    public List<CreditCard> getCreditCards() {
//        return creditCardRepository.findAll();
//    }


    @PostMapping("/addMemberCreditCard")
    public String addMemberCreditCard(String cardholder_name, String card_number, String card_type,
                                      int expiration_month, int expiration_year, String security_code, Model model){
        User user = userSession.getUser();
        model.addAttribute("user", userSession.getUser());
        CreditCard newCard = new CreditCard(cardholder_name, card_number, card_type, expiration_month, expiration_year, security_code);
        if(user!=null){
            newCard.setUser(user);
            user.getCredit_cards().add(newCard);
        }
        else{
            model.addAttribute("error", "\nError, No Member logged in to save card details.");
            return "login.html";
        }

        newCard = creditCardRepository.saveAndFlush(newCard);
        return "viewProfile.html";
    }

    @GetMapping("/viewMemberCreditCards")
    public String viewMemberCreditCards(Model model){
        model.addAttribute("user", userSession.getUser());
        model.addAttribute("cards", creditCardRepository.findAllByUser(userSession.getUser()));
        return "viewCreditCards.html";
    }

    @GetMapping("/registerCard")
    public String registerCardView(Model model){

        model.addAttribute("user", userSession.getUser());
        return "registerCreditCard.html";
    }

    @GetMapping("/deleteCard/{id}")
    public String deleteCard(@PathVariable(value = "id") Long id, Model model) throws CreditCardNotFoundException {
        CreditCard card = creditCardRepository.findById(id)
                .orElseThrow(() -> new CreditCardNotFoundException(id));

        creditCardRepository.delete(card);
        model.addAttribute("user", userSession.getUser());
        model.addAttribute("cards", creditCardRepository.findAllByUser(userSession.getUser()));

        return "viewCreditCards.html";
    }

}
