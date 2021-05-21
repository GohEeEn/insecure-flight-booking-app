package ucd.comp40660.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ucd.comp40660.flight.repository.FlightRepository;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.service.UserService;
import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.exception.UserNotFoundException;
import ucd.comp40660.user.model.Role;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.CreditCardRepository;
import ucd.comp40660.user.repository.UserRepository;
import ucd.comp40660.validator.UserValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Iterator;
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

    @Autowired
    protected AuthenticationManager authenticationManager;


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

            StringBuilder userRoles = new StringBuilder();
            for (Role role : userRepository.findByUsername(sessionUser.getUsername()).getRoles()) {
                userRoles.append(role.getName());
            }

            LOGGER.info("List all users called by <" + sessionUser.getUsername() + "> with the role of <" + userRoles + ">");
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

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(userSession.getUser().getUsername()).getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Called get a registration by id <" + username + "> from <" + userSession.getUser().getUsername() + "> with the role of <" + userRoles + ">");
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

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(user.getUsername()).getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Successfully updated registration details for user <" + user.getUsername() + "> with the role of <" + userRoles + ">");

        return userRepository.save(user);
    }

    //    Delete a registration record
    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @GetMapping("/user/delete/{username}")
    public String deleteRegistration(@PathVariable(value = "username") String username, HttpServletRequest req) throws UserNotFoundException {
        Principal userDetails = req.getUserPrincipal();
        User sessionUser = userRepository.findByUsername(userDetails.getName());
        User user = userRepository.findByUsername(username);

//        User user = userRepository.findById(registrationID)
//                .orElseThrow(() -> new UserNotFoundException(registrationID));

        userRepository.delete(user);

        LOGGER.info("Successfully deleted user registration for user <" + username + "> by admin <" + userSession.getUser().getUsername() + ">");

        if (sessionUser.getUsername().equals(user.getUsername())) {
            userSession.setUser(null);
        }

        //TODO Possible Session management after account deletion?

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
    public String register(@Valid @ModelAttribute("userForm") User userForm, BindingResult bindingResult) {
        userValidator.validate(userForm, bindingResult);

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userForm.getRoles()) {
            userRoles.append(role.getName());
        }

        if (bindingResult.hasErrors()) {

            LOGGER.warn("Unable to register user with username <" + userForm.getUsername() + "> with the role of <" + userRoles + ">");
            return "register.html";
        }

        LOGGER.info("New user registered with username <" + userForm.getUsername() + "> with the role of <" + userRoles + ">");
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
    public String adminRegister(@Valid @ModelAttribute("userForm") User userForm, BindingResult bindingResult) {
        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            LOGGER.warn("New admin could not be registered with username <" + userForm.getUsername() + ">");
            return "adminRegister.html";
        }

        LOGGER.warn("New admin registered with username <" + userForm.getUsername() + ">");
        userService.adminSave(userForm);

        return "index.html";
    }

    @GetMapping("/guestRegister")
    public String guestRegister(Model model, HttpServletResponse response) throws Exception {
        if (userSession.isLoginFailed()) {
            model.addAttribute("error", "Unable to create account, passwords do not match");
            userSession.setLoginFailed(false);
        }
        if (userSession.getUser() != null) {
            response.sendRedirect("/logout");
        }
        return "guestRegister.html";
    }

    @PostMapping("/guestRegister")
    public String guestRegister(Model model, @ModelAttribute("userForm") User userForm, BindingResult bindingResult){
        userValidator.validate(userForm, bindingResult);

        if(bindingResult.hasErrors()){
            model.addAttribute("error", bindingResult.getAllErrors().toString());
            return "guestRegister.html";
        }

        userService.guestSave(userForm);

        return "index.html";
    }

    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @GetMapping("/viewProfile/{username}")
    public String viewProfile(@PathVariable(value = "username") String username, Model model, HttpServletRequest req) {
        User user = null;
        User sessionUser = null;

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(username).getRoles()) {
            userRoles.append(role.getName());
        }

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        user = userRepository.findByUsername(username);
        model.addAttribute("user", user);
        return "viewProfile.html";
    }

    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @GetMapping("/editProfile/{username}")
    public String loadEditProfile(@PathVariable(value = "username") String username, Model model, HttpServletRequest req) {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        //Determine if booking as admin
        User user = null;
        if(isAdmin(sessionUser)){
            user = userRepository.findByUsername(username);
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);

//        model.addAttribute("user", userSession.getUser());
        return "editProfile.html";
    }

    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @PostMapping("/editProfile/{username}")
    public String editProfile(@PathVariable(value = "username") String username, String newName, String newSurname,
                              String newPhone, String newEmail, String newAddress, String newUsername, String password,
                              HttpServletRequest req, Model model) throws Exception {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        //Determine if booking as admin
        User user = null;
        if(isAdmin(sessionUser)){
            user = userRepository.findByUsername(username);
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);

        StringBuilder userRoles = new StringBuilder();
        for (Role role : user.getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Profile edited by user <" + user.getUsername() + "> with the role of <" + userRoles + ">");

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

                if (!user.getUsername().equals(newUsername) && userService.findByUsername(newUsername) == null)
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
            LOGGER.warn("Unsuccessful attempt of profile edit for user <" + user.getUsername() + "> with the role of <" + userRoles + ">");
            return "editProfile.html";
        }
    }

    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @GetMapping("/editPassword/{username}")
    public String changePassword(@PathVariable(value = "username") String username, Model model, HttpServletRequest req) {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        //Determine if booking as admin
        User user = null;
        if(isAdmin(sessionUser)){
            user = userRepository.findByUsername(username);
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);

        return "editPassword.html";
    }

//    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
    @PostMapping("/editPassword")
    public String editPassword(String username, String password, String newPassword, String newPasswordDuplicate,
                               HttpServletRequest req, Model model)
            throws Exception {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        //Determine if booking as admin
        User user = null;
        if(isAdmin(sessionUser)){
            user = userRepository.findByUsername(username);
        }
        else{
            user = sessionUser;
        }
        model.addAttribute("user", user);

        StringBuilder userRoles = new StringBuilder();
        for (Role role : user.getRoles()) {
            userRoles.append(role.getName());
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

        // Check if the current password matches given current password input
        if (bCryptPasswordEncoder.matches(password, user.getPassword())) {

            // Check if the new password matches the passwordConfirm
            if (newPassword.equals(newPasswordDuplicate)) {

                // Check if the new password matches the policy pattern
                if(userValidator.isPasswordValid(newPassword)) {

                    user.setPassword(newPassword);
                    userRepository.save(user);
                    model.addAttribute("user", userSession.getUser());

                    LOGGER.info("Password successfully changed by user <" + user.getUsername() + ">");

                    return "viewProfile.html";

                } else {
                    model.addAttribute("error", "\nInvalid password, update denied.");
                    LOGGER.warn("Password change rejected due to invalid password by user <" + user.getUsername() + ">");
                }

            } else {
                model.addAttribute("error", "\nNew Password entries do not match, update denied.");
                model.addAttribute("user", userSession.getUser());
                LOGGER.warn("Password change rejected due to new password mismatch for user <" + user.getUsername() + "> with role of <" + userRoles + ">");
            }

        } else {
            System.out.println("\n\nPASSWORD FOUND TO BE INCORRECT, given : " + password + ", actual : " + user.getPassword() + "\n\n");
            model.addAttribute("user", userSession.getUser());
            model.addAttribute("error", "\nIncorrect Password, alterations denied.");
            LOGGER.warn("Incorrectly entered password for user <" + user.getUsername() + "> with role of <" + userRoles + ">");
        }

        return "editPassword.html";
    }

    private boolean isAdmin(User sessionUser) {
        boolean isAdmin = false;
        Iterator<Role> roleIterator = sessionUser.getRoles().iterator();
        while(roleIterator.hasNext()){
            if(roleIterator.next().getName().equals("ADMIN")){
                isAdmin = true;
            }
        }
        return isAdmin;
    }

    private boolean isGuest(User sessionUser) {
        boolean isGuest = false;
        Iterator<Role> roleIterator = sessionUser.getRoles().iterator();
        while(roleIterator.hasNext()){
            if(roleIterator.next().getName().equals("GUEST")){
                isGuest = true;
            }
        }
        return isGuest;
    }

}
