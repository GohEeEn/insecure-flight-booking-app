package ucd.comp40660.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import ucd.comp40660.service.EncryptionService;
import ucd.comp40660.user.UserSession;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import ucd.comp40660.user.exception.CreditCardNotFoundException;
import ucd.comp40660.user.model.CreditCard;
import ucd.comp40660.user.model.Role;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.CreditCardRepository;
import ucd.comp40660.user.repository.GuestRepository;
import ucd.comp40660.user.repository.UserRepository;
import ucd.comp40660.validator.CreditCardValidator;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
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

    @Autowired
    CreditCardValidator creditCardValidator;


    @PostMapping("/addMemberCreditCard")
    public String addMemberCreditCard(@Valid @ModelAttribute("cardForm") CreditCard cardForm,
                                      Model model, HttpServletRequest req, BindingResult bindingResult) {


        User user = null;
        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
        user = userRepository.findByUsername(userDetails.getName());
        sessionUser = user;
        }

        model.addAttribute("user", user);
        model.addAttribute("sessionUser", sessionUser);

        creditCardValidator.validate(cardForm, bindingResult);

        if(bindingResult.hasErrors()){
            return "registerCreditCard.html";
        }

        // encrypt card_number and security_code before saving it in the database
        String encryptedCardholderName = EncryptionService.encrypt(cardForm.getCardholder_name());
        String encryptedCardNumber = EncryptionService.encrypt(cardForm.getCard_number());
        String encryptedCardType = EncryptionService.encrypt(cardForm.getType());
        String encryptedSecurityCode = EncryptionService.encrypt(cardForm.getSecurity_code());

//        User user = userSession.getUser();
        model.addAttribute("user", user);
        CreditCard newCard = new CreditCard(encryptedCardholderName, encryptedCardNumber, encryptedCardType,
                cardForm.getExpiration_month(), cardForm.getExpiration_year(), encryptedSecurityCode);
        if (user != null) {
            newCard.setUser(user);
            user.getCredit_cards().add(newCard);
        } else {
            LOGGER.warn("Unsuccessful attempt to add credit card details by a non-logged in user.");
            model.addAttribute("error", "\nError, No Member logged in to save card details.");
            return "login.html";
        }

        newCard = creditCardRepository.saveAndFlush(newCard);

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(user.getUsername()).getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Member credit card added for user <" + user.getUsername() + "> with the role of <" + userRoles + ">");

        return "viewProfile.html";
    }


    @PreAuthorize("#username == authentication.name")
    @GetMapping("/viewCreditCards/{username}")
    public String viewMemberCreditCards(@PathVariable(value = "username") String username, Model model, HttpServletRequest req) {
        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            List<CreditCard> creditCards = sessionUser.getCredit_cards();

            for (CreditCard card : creditCards) {
                card.setCardholder_name(EncryptionService.decrypt(card.getCardholder_name()));
                card.setCard_number(EncryptionService.decrypt(card.getCard_number()));
                card.setType(EncryptionService.decrypt(card.getType()));
                card.setSecurity_code(EncryptionService.decrypt(card.getSecurity_code()));
            }

            sessionUser.setCredit_cards(creditCards);

            model.addAttribute("sessionUser", sessionUser);
            model.addAttribute("user", sessionUser);
        }

        model.addAttribute("cards", creditCardRepository.findAllByUser(sessionUser));

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(username).getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Called viewCreditCards(): by user <" + username + "> with the role of <" + userRoles + ">");

        return "viewCreditCards.html";
    }

    @PreAuthorize("#username == authentication.name")
    @GetMapping("/registerCard/{username}")
    public String registerCardView(@PathVariable(value = "username") String username, Model model, @Valid @ModelAttribute("cardForm") CreditCard cardForm, HttpServletRequest req) {
        User user = null;


        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            user = userRepository.findByUsername(userDetails.getName());
            User sessionUser = user;
            model.addAttribute("user", user);
            model.addAttribute("sessionUser", sessionUser);
        }


//        model.addAttribute("user", userSession.getUser());
        return "registerCreditCard.html";
    }

    @PreAuthorize("#username == authentication.name")
    @GetMapping("/deleteCard/{username}/{id}")
    public void deleteCard(@PathVariable(value = "id") Long id, @PathVariable(value = "username") String username, Model model, HttpServletRequest req, HttpServletResponse response) throws CreditCardNotFoundException, IOException {
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

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(user.getUsername()).getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Deleted credit card by user <" + user.getUsername() + "> with the role of <" + userRoles + ">");

        response.sendRedirect("/viewCreditCards/" + user.getUsername());
    }
}
