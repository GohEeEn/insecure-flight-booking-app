package ucd.comp40660.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ucd.comp40660.flight.repository.FlightRepository;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.service.UserService;
import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.exception.UserNotFoundException;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.CreditCardRepository;
import ucd.comp40660.user.repository.UserRepository;
import ucd.comp40660.validator.UserValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;


@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

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

    @Autowired
    UserValidator userValidator;

    @Autowired
    UserService userService;


    @GetMapping("/")
    public String index(Model model, HttpServletRequest req) {
        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            User sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        return "index.html";
    }

    //    Get all registrations
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public String getAllUsers(Model model, HttpServletRequest req) {
        Principal userDetails = req.getUserPrincipal();

        if (userDetails != null) {
            User sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
            LOGGER.info("List all users called by <" + sessionUser.getUsername() + "> with the role of <" + sessionUser.getRoles() + ">");
        }

        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);

        return "adminViewUsers.html";
    }

    //    Get a single registration by id
//    the id can be changed to any other attribute
    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @GetMapping("/users/{username}")
    @ResponseBody
    public User getRegistrationByUsername(@PathVariable(value = "username") String username, HttpServletRequest req) throws UserNotFoundException {
//        Principal userDetails = req.getUserPrincipal();

        LOGGER.info("Called get a registration by id <" + username + "> from <" + userSession.getUser().getUsername() + "> with the role of <" + userSession.getUser().getRoles() + ">");
        return userRepository.findByUsername(username);
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

        LOGGER.info("Successfully updated registration details for user <" + user.getUsername() + "> with the role of <" + user.getRoles() + ">");

        return userRepository.save(user);
    }

    //    Delete a registration record
    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @GetMapping("/delete/{username}")
    public String deleteRegistration(@PathVariable(value = "username") String username, HttpServletRequest req) throws UserNotFoundException {
        Principal userDetails = req.getUserPrincipal();
        User sessionUser = userRepository.findByUsername(userDetails.getName());
        User user = userRepository.findByUsername(username);

//        User user = userRepository.findById(registrationID)
//                .orElseThrow(() -> new UserNotFoundException(registrationID));

        LOGGER.info("Successfully deleted user registration for user <" + username + "> by admin <" + userSession.getUser().getUsername() + ">");

        userRepository.delete(user);

        if (sessionUser.getUsername().equals(user.getUsername())) {
            userSession.setUser(null);
        }

        return "index.html";
    }

    @GetMapping("/register")
    public String register(Model model, @ModelAttribute("userForm") User userForm, HttpServletResponse response) throws Exception {
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
    public String register(@Valid @ModelAttribute("userForm") User userForm, BindingResult bindingResult){
        userValidator.validate(userForm, bindingResult);

        if(bindingResult.hasErrors()){
            LOGGER.warn("Unable to register user with username <" + userForm.getUsername() + "> with the role of <" + userForm.getRoles());
            return "register.html";
        }

        LOGGER.info("New user registered with username <" + userForm.getUsername() + "> with the role of <" + userForm.getRoles() + ">");
        userService.save(userForm);

        return "index.html";
    }

    @GetMapping("/adminRegister")
    public String adminRegister(Model model, @ModelAttribute("userForm") User userForm, HttpServletResponse response) throws Exception {
        if (userSession.isLoginFailed()) {
            model.addAttribute("error", "Unable to create account, passwords do not match");
            userSession.setLoginFailed(false);
        }
        if (userSession.getUser() != null) {
            response.sendRedirect("/logout");
        }
        return "adminRegister.html";
    }

    @PostMapping("/adminRegister")
    public String adminRegister(@Valid @ModelAttribute("userForm") User userForm, BindingResult bindingResult){
        userValidator.validate(userForm, bindingResult);

        if(bindingResult.hasErrors()){
            LOGGER.warn("New admin could not be registered for user <" + userForm.getUsername() + ">");
            return "adminRegister.html";
        }

        LOGGER.warn("New admin registered with username <" + userForm.getUsername() + ">");
        userService.adminSave(userForm);

        return "index.html";
    }


//    @PostMapping("/register")
//    public String createUser(String name, String surname, String username, String phone, String address, String email,
//                             String password, String passwordDuplicate, Model model) throws SQLIntegrityConstraintViolationException, IOException {
//
//        if (userRepository.existsByUsername(username)) {
//            System.out.println("\n\nDUPLICATE USERNAME DETECTED\n\n");
//            model.addAttribute("error", "Username already exists.");
//            return "register.html";
//        } else if (userRepository.existsByEmail(email)) {
//            model.addAttribute("error", "E-mail address already in use.");
//            return "register.html";
//        } else if (userRepository.existsByPhone(phone)) {
//            model.addAttribute("error", "Phone number already in use.");
//            return "register.html";
//        } else {
//            if (password.equals(passwordDuplicate)) {
//                User user = new User();
//                user.setName(name);
//                user.setSurname(surname);
//                user.setUsername(username);
//                user.setPhone(phone);
//                user.setAddress(address);
//                user.setEmail(email);
//                user.setRole("MEMBER");
//                user.setPassword(password);
//                userRepository.save(user);
//                userSession.setUser(user);
//                return "index.html";
//            } else {
//                userSession.setLoginFailed(true);
//                return "register.html";
//            }
//        }
//    }

    @GetMapping("/viewProfile")
    public String viewProfile(Model model, HttpServletRequest req) {
        Principal userDetails = req.getUserPrincipal();
        User user = userRepository.findByUsername(userDetails.getName());

        model.addAttribute("user", user);
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

        LOGGER.info("Profile edited by user <" + user.getUsername() + "> with the role of <" + user.getRoles() + ">");

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
            if (!(newUsername.isEmpty())) {

                if(!user.getUsername().equals(newUsername) && userService.findByUsername(newUsername) == null)
                    user.setUsername(newUsername);
                else {
                    System.out.println("\n\nINVALID USERNAME <" + newUsername + "> \n\n");
                    model.addAttribute("user", userSession.getUser());
                    model.addAttribute("error", "\nInvalid Username, alterations denied.");
                    return "editProfile.html";
                }

            } else {
                user.setUsername(user.getUsername());
            }

            userRepository.save(user);

            model.addAttribute("user", userSession.getUser());

            return "viewProfile.html";

        } else {
            System.out.println("\n\nPASSWORD FOUND TO BE INCORRECT\n\n");
            model.addAttribute("user", userSession.getUser());
            model.addAttribute("error", "\nIncorrect Password, alterations denied.");
            LOGGER.warn("Unsuccessful attempt of profile edit for user <" + user.getUsername() + "> with the role of <" + user.getRoles() + ">");
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
                LOGGER.warn("Password change rejected due to new password mismatch for user <" + user.getUsername() + "> with role of <" + user.getRoles() + ">");

                return "editPassword.html";
            }

            userRepository.save(user);
            model.addAttribute("user", userSession.getUser());

            LOGGER.info("Password successfully changed by user <" + user.getUsername() + ">");

            return "viewProfile.html";

        } else {
            System.out.println("\n\nPASSWORD FOUND TO BE INCORRECT\n\n");
            model.addAttribute("user", userSession.getUser());
            model.addAttribute("error", "\nIncorrect Password, alterations denied.");
            LOGGER.warn("Incorrectly entered password for user <" + user.getUsername() + "> with role of <" + user.getRoles() + ">");
        }

        return "editPassword.html";
    }
}
