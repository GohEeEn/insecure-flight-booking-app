package ucd.comp40660.user.controller;

import ucd.comp40660.flight.model.Flight;
import ucd.comp40660.flight.repository.FlightRepository;
import ucd.comp40660.reservation.exception.ReservationNotFoundException;
import ucd.comp40660.reservation.model.Reservation;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.user.UserSession;
import org.springframework.stereotype.Controller;
import ucd.comp40660.user.exception.CreditCardNotFoundException;
import ucd.comp40660.user.exception.UserNotFoundException;
import ucd.comp40660.user.model.CreditCard;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.CreditCardRepository;
import ucd.comp40660.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;


import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;


@Controller
public class UserController {

    @Autowired
    private UserSession userSession;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CreditCardRepository creditCardRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    FlightRepository flightRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("user", userSession.getUser());
        return "index.html";
    }

    //    Get all registrations
    @GetMapping("/users")
    @ResponseBody
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //    Get a single registration by id
//    the id can be changed to any other attribute
    @GetMapping("/users/{id}")
    @ResponseBody
    public User getRegistrationById(@PathVariable(value = "id") Long registrationId) throws UserNotFoundException {
        return userRepository.findById(registrationId)
                .orElseThrow(() -> new UserNotFoundException(registrationId));
    }

    //    update registration details
    @PutMapping("/users/{id}")
    public User updateRegistration(@PathVariable(value = "id") Long registrationId, @Valid @RequestBody User userDetails) throws UserNotFoundException {
        User user = userRepository.findById(registrationId)
                .orElseThrow(() -> new UserNotFoundException(registrationId));

//        update the details of a registration record
        user.setAddress(userDetails.getAddress());
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setSurname(user.getSurname());
        user.setCredit_card_details(userDetails.getCredit_card_details());
//        user.setUpcoming_reservations(userDetails.getUpcoming_reservations());
//        user.setReservation_history(user.getReservation_history());

        User updatedUser = userRepository.save(user);

        return updatedUser;
    }

    //    Delete a registration record
    @GetMapping("/delete/{id}")
    public String deleteRegistration(@PathVariable(value = "id") Long registrationID) throws UserNotFoundException {
        User user = userRepository.findById(registrationID)
                .orElseThrow(() -> new UserNotFoundException(registrationID));

        userRepository.delete(user);
        userSession.setUser(null);

        return "index.html";
    }

    @GetMapping("/register")
    public String register(Model model, HttpServletResponse response) throws Exception {
        if (userSession.isLoginFailed()) {
            model.addAttribute("error", "Unable to create account, passwords do not match");
            userSession.setLoginFailed(false);
        }
        if (userSession.getUser() != null) {
            response.sendRedirect("/logout");
        }
        return "register.html";
    }

    @PostMapping("/register")
//    public User createUser(@Valid @RequestParam User user) {
//        return userRepository.save(user);
//    }
    public String createUser(String name, String surname, String username, String phone, String address, String email, String credit_card_details,
                             String password, String passwordDuplicate, HttpServletResponse response, Model model) throws SQLIntegrityConstraintViolationException, IOException {

        if (userRepository.existsByUsername(username)) {
            System.out.println("\n\nDUPLICATE USERNAME DETECTED\n\n");
            model.addAttribute("error", "Username already exists.");
//            response.sendRedirect("/register");
            return "register.html";
        } else if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "E-mail address already in use.");
            return "register.html";
        } else if (userRepository.existsByPhone(phone)) {
            model.addAttribute("error", "Phone number already in use.");
            return "register.html";
        } else {
            if (password.equals(passwordDuplicate)) {
                User user = new User();
                user.setName(name);
                user.setSurname(surname);
                user.setUsername(username);
                user.setPhone(phone);
                user.setAddress(address);
                user.setEmail(email);
                user.setCredit_card_details(credit_card_details);
                user.setRole("member");
                user.setPassword(password);
//                user.setReservation_history("None");
//                user.setUpcoming_reservations("None");
                userRepository.save(user);
                userSession.setUser(user);
//                response.sendRedirect("/");
                return "index.html";

            } else {
                userSession.setLoginFailed(true);
//                response.sendRedirect("/register");
                return "register.html";

            }
        }
    }

    @GetMapping("/viewProfile")
    public String viewProfile(Model model) {
        model.addAttribute("user", userSession.getUser());
        return "viewProfile.html";
    }

    @GetMapping("/editProfile")
    public String loadEditProfile(Model model) {
        model.addAttribute("user", userSession.getUser());
        return "editProfile.html";
    }

    @PostMapping("/editProfile")
    public String editProfile(String newName, String newSurname, String newPhone, String newEmail, String newAddress, String newCreditCardDetails,
                              String newUsername, String password, Model model) throws Exception {

        User user = userSession.getUser();

        if (password.equals(user.getPassword())) {

            if (!(newName.isEmpty())) {
                user.setName(newName);
            } else {
                user.setName(user.getName());
            }
            if (!(newSurname.isEmpty())) {
                user.setSurname(newSurname);
            } else {
                user.setSurname(user.getSurname());
            }
            if (!(newAddress.isEmpty())) {
                user.setAddress(newAddress);
            } else {
                user.setAddress(user.getAddress());
            }
            if (!(newEmail.isEmpty())) {
                user.setEmail(newEmail);
            } else {
                user.setEmail(user.getEmail());
            }
            if (!(newPhone.isEmpty())) {
                user.setPhone(newPhone);
            } else {
                user.setPhone(user.getPhone());
            }
            if (!(newCreditCardDetails.isEmpty())) {
                user.setCredit_card_details(newCreditCardDetails);
            } else {
                user.setCredit_card_details(user.getCredit_card_details());
            }
            if (!(newUsername.isEmpty())) {
                user.setUsername(newUsername);
            } else {
                user.setUsername(user.getUsername());
            }

//            user.setUpcoming_reservations(user.getUpcoming_reservations());
//            user.setReservation_history(user.getReservation_history());
            userRepository.save(user);

            model.addAttribute("user", userSession.getUser());

            return "viewProfile.html";

        } else {
            System.out.println("\n\nPASSWORD FOUND TO BE INCORRECT\n\n");
            model.addAttribute("user", userSession.getUser());
            model.addAttribute("error", "\nIncorrect Password, alterations denied.");
            return "editProfile.html";
        }
    }

    @GetMapping("/editPassword")
    public String changePassword(Model model) {
        model.addAttribute("user", userSession.getUser());
        return "editPassword.html";
    }


    @PostMapping("/editPassword")
    public String editPassword(String password, String newPassword, String newPasswordDuplicate, HttpServletResponse response, Model model)
            throws Exception {

        User user = userSession.getUser();

        if (password.equals(user.getPassword())) {

            if (newPassword.equals(newPasswordDuplicate) && (!(newPassword.isEmpty()))) {
                user.setPassword(newPassword);
            } else {
                model.addAttribute("error", "\nNew Password entries do not match, update denied.");
                model.addAttribute("user", userSession.getUser());

                return "editPassword.html";
            }

            userRepository.save(user);
            model.addAttribute("user", userSession.getUser());

            return "viewProfile.html";

        } else {
            System.out.println("\n\nPASSWORD FOUND TO BE INCORRECT\n\n");
            model.addAttribute("user", userSession.getUser());
            model.addAttribute("error", "\nIncorrect Password, alterations denied.");
        }

        return "editPassword.html";
    }

    @GetMapping("/cards")
    @ResponseBody
    public List<CreditCard> getCreditCards() {
        return creditCardRepository.findAll();
    }


    @PostMapping("/addMemberCreditCard")
    public String addMemberCreditCard(String cardholder_name, String card_number, String card_type,
                                      int expiration_month, int expiration_year, String security_code, Model model) {
        User user = userSession.getUser();
        model.addAttribute("user", userSession.getUser());
        CreditCard newCard = new CreditCard(cardholder_name, card_number, card_type, expiration_month, expiration_year, security_code);
        if (user != null) {
            newCard.setUser(user);
        } else {
            model.addAttribute("error", "\nError, No Member logged in to save card details.");
            return "login.html";
        }

        creditCardRepository.save(newCard);
        return "viewProfile.html";
    }

    @GetMapping("/viewMemberCreditCards")
    public String viewMemberCreditCards(Model model) {
        model.addAttribute("user", userSession.getUser());
        model.addAttribute("cards", creditCardRepository.findAllByUser(userSession.getUser()));
        return "viewCreditCards.html";
    }

    @GetMapping("/registerCard")
    public String registerCardView(Model model) {

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

    @GetMapping("/getUserReservations")
    public String getUserReservations(Model model) throws ReservationNotFoundException {
        if (userSession.getUser() != null) {
            User user = userSession.getUser();

//            add current user to the model
            model.addAttribute("user", user);

//            find all reservations associated with a user
            List<Reservation> reservations = reservationRepository.findAllByUser(user);

            if (reservations.size() > 0) {

//            loop through each reservation and find the flights associated
                List<Flight> flights = new ArrayList<>();

                for (Reservation reservation : reservations) {
                    Flight flight = flightRepository.findFlightByReservation(reservation);

                    if (flight != null) {
                        flights.add(flight);
                    }
                }

//            add the flights to the model, so thymeleaf can display them
                model.addAttribute("flightsUser", flights);

//            display the reservations on a new page
                return "viewFlightsUser";
            } else { // throw an error if there are no reservations
                throw new ReservationNotFoundException();
            }
        }

//        if user doesn't have any reservations (past/future)
//        bring them back to their profile page
        return "viewProfile";

    }

}
