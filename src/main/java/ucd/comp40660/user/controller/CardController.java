package ucd.comp40660.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
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


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;


@Controller
public class CardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardController.class);

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
                                      int expiration_month, int expiration_year, String security_code, Model model, HttpServletRequest req) {
        User user = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            user = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("user", user);
        }

//        User user = userSession.getUser();
        model.addAttribute("user", user);
        CreditCard newCard = new CreditCard(cardholder_name, card_number, card_type, expiration_month, expiration_year, security_code);
        if (user != null) {
            newCard.setUser(user);
            user.getCredit_cards().add(newCard);
        } else {
            LOGGER.warn("%s", "Unsuccessful attempt to add credit card details by a non-logged in user.");
            model.addAttribute("error", "\nError, No Member logged in to save card details.");
            return "login.html";
        }

        newCard = creditCardRepository.saveAndFlush(newCard);

        LOGGER.info("%s", "Member credit card added for user <" + user.getUsername() + "> with the role of <" + user.getRoles() + ">");
        return "viewProfile.html";
    }

    @PreAuthorize("#username == authentication.name")
    @GetMapping("/viewCreditCards/{username}")
    public String viewMemberCreditCards(@PathVariable(value = "username") String username, Model model, HttpServletRequest req) {
        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        model.addAttribute("cards", creditCardRepository.findAllByUser(sessionUser));

        LOGGER.info("%s", "Called viewCreditCards(): by user <" + username + "> with the role of <" + userRepository.findByUsername(username).getRoles() + ">");
        return "viewCreditCards.html";
    }

    @GetMapping("/registerCard")
    public String registerCardView(Model model, HttpServletRequest req) {
        User user = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            user = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("user", user);
        }


//        model.addAttribute("user", userSession.getUser());
        return "registerCreditCard.html";
    }

    @GetMapping("/deleteCard/{id}")
    public String deleteCard(@PathVariable(value = "id") Long id, Model model, HttpServletRequest req) throws CreditCardNotFoundException {
        User user = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            user = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("user", user);
        }

        CreditCard card = creditCardRepository.findById(id)
                .orElseThrow(() -> new CreditCardNotFoundException(id));

        creditCardRepository.delete(card);
        model.addAttribute("user", user);
        model.addAttribute("cards", creditCardRepository.findAllByUser(user));

        LOGGER.info("%s", "Deleted credit card by user <" + user.getUsername() + "> with the role of <" + user.getRoles() + ">");

        return "viewCreditCards.html";
    }

}
