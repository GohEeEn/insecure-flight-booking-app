package ucd.comp40660.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ucd.comp40660.flight.repository.FlightRepository;
import ucd.comp40660.reservation.repository.ReservationRepository;
import ucd.comp40660.service.SecurityServiceImplementation;
import ucd.comp40660.service.UserService;
import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.exception.UserNotFoundException;
import ucd.comp40660.user.model.*;
import ucd.comp40660.user.model.Role;
import ucd.comp40660.user.repository.CreditCardRepository;
import ucd.comp40660.user.repository.UserRepository;
import ucd.comp40660.validator.PasswordValidator;
import ucd.comp40660.validator.ProfileUpdateValidator;
import ucd.comp40660.validator.UserValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;

import static ucd.comp40660.filter.SecurityConstants.LOGIN_URL;


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
    ProfileUpdateValidator profileUpdateValidator;

    @Autowired
    PasswordValidator passwordValidator;

    @Autowired
    UserService userService;

    @Autowired
    protected AuthenticationManager authenticationManager;

    @Autowired
    SecurityServiceImplementation securityService;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;



    @GetMapping("/")
    public String index(Model model, HttpServletRequest req, HttpServletResponse response) throws IOException {
        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            User sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }
        else{
            securityService.guestLogin();
            userDetails = req.getUserPrincipal();
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

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(userSession.getUser().getUsername()).getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("Called get a registration by id <" + username + "> from <" + userSession.getUser().getUsername() + "> with the role of <" + userRoles + ">");
        return userRepository.findByUsername(username);
    }

    //    update registration details
    @PreAuthorize("#username == authentication.name or hasAuthority('ADMIN')")
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
    @PostMapping("/user/delete")
    public void deleteRegistration(@RequestParam String username, HttpServletRequest req, HttpServletResponse response) throws UserNotFoundException, IOException {
        Principal userDetails = req.getUserPrincipal();
        User sessionUser = userRepository.findByUsername(userDetails.getName());
        User user = userRepository.findByUsername(username);

        userRepository.delete(user);

        if (sessionUser.getUsername().equals(user.getUsername())) {
            userSession.setUser(null);
        }

        response.sendRedirect(LOGIN_URL);

    }


    @GetMapping("/register")
    public String register(Model model, @Valid @ModelAttribute("userForm") User userForm, HttpServletResponse response) throws Exception {
        return "register.html";
    }


    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userForm") User userForm, BindingResult bindingResult,
                           Model model, HttpServletRequest req) throws ServletException {

        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {

            LOGGER.warn("Unable to register user with username <" + userForm.getUsername() + ">");
            return "register.html";
        }

        userService.save(userForm);

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userRepository.findByUsername(userForm.getUsername()).getRoles()) {
            userRoles.append(role.getName());
        }
        LOGGER.info("New user registered with username <" + userForm.getUsername() + "> with authority <" + userRoles + ">");

//        Principal userDetails = req.getUserPrincipal();
        securityService.guestLogin();
        User sessionUser = userRepository.findByUsername("testguest");
        model.addAttribute("sessionUser", sessionUser);

        return "index.html";
    }


    @PreAuthorize("hasAuthority('ADMIN')")
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

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/adminRegister")
    public String adminRegister(@Valid @ModelAttribute("userForm") User userForm, BindingResult bindingResult,
                                Model model, HttpServletRequest req) throws ServletException {
        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            LOGGER.warn("New admin could not be registered with username <" + userForm.getUsername() + ">");
            return "adminRegister.html";
        }

        LOGGER.warn("New admin registered with username <" + userForm.getUsername() + ">");
        userService.adminSave(userForm);
        securityService.autoLogin(userForm.getUsername(), userForm.getPassword(), req);


        Principal userDetails = req.getUserPrincipal();
        User sessionUser = userRepository.findByUsername(userDetails.getName());
        model.addAttribute("sessionUser", sessionUser);


        return "index.html";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/guestRegister")
    public String guestRegister(Model model, HttpServletResponse response,
                                @Valid @ModelAttribute("userForm") User userForm) throws Exception {
        if (userSession.isLoginFailed()) {
            model.addAttribute("error", "Unable to create account, passwords do not match");
            userSession.setLoginFailed(false);
        }
        if (userSession.getUser() != null) {
            response.sendRedirect("/logout");
        }
        return "guestRegister.html";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/guestRegister")
    public String guestRegister(Model model, @ModelAttribute("userForm") User userForm,
                                BindingResult bindingResult, HttpServletRequest req) throws ServletException {
        userValidator.validate(userForm, bindingResult);

        if(bindingResult.hasErrors()){
            model.addAttribute("error", bindingResult.getAllErrors().toString());
            return "guestRegister.html";
        }

        userService.guestSave(userForm);
        securityService.autoLogin(userForm.getUsername(), userForm.getPasswordConfirm(), req);

        Principal userDetails = req.getUserPrincipal();
        User sessionUser = userRepository.findByUsername(userDetails.getName());
        model.addAttribute("sessionUser", sessionUser);

        LOGGER.info("New guest registered with the username <" + userForm.getUsername() + ">");

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


    @PreAuthorize("#username == authentication.name")
    @GetMapping("/editProfile/{username}")
    public String loadEditProfile(@PathVariable(value = "username") String username,
                                  @ModelAttribute("userForm") User userForm,
                                  Model model, HttpServletRequest req) {

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


    @PreAuthorize("#username == authentication.name")
    @PostMapping("/editProfile/{username}")
    public String editProfile(@PathVariable("username") String username,
                              @Valid @ModelAttribute("userForm") User userForm, BindingResult bindingResult,
                              HttpServletRequest req, Model model) throws Exception {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        //Determine if booking as admin
        User user;

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

        profileUpdateValidator.validate(userForm, bindingResult);

        if(bindingResult.hasErrors()){
            return "editProfile.html";
        }

        LOGGER.info("Profile edited by user <" + user.getUsername() + "> with the role of <" + userRoles + ">");

        bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

        if(bCryptPasswordEncoder.matches(userForm.getPasswordConfirm(), user.getPassword())){

            if (!(userForm.getName().isEmpty())) {
                user.setName(userForm.getName());
            } else {
                user.setName(user.getName());
            }
            if (!(userForm.getSurname().isEmpty())) {
                user.setSurname(userForm.getSurname());
            } else {
                user.setSurname(user.getSurname());
            }
            if (!(userForm.getAddress().isEmpty())) {
                user.setAddress(userForm.getAddress());
            } else {
                user.setAddress(user.getAddress());
            }
            if (!(userForm.getEmail().isEmpty())) {
                user.setEmail(userForm.getEmail());
            } else {
                user.setEmail(user.getEmail());
            }
            if (!(userForm.getPhone().isEmpty())) {
                user.setPhone(userForm.getPhone());
            } else {
                user.setPhone(user.getPhone());
            }
            if (!(userForm.getUsername().isEmpty())) {

                if (!user.getUsername().equals(userForm.getUsername()) && userService.findByUsername(userForm.getUsername()) == null)
                    user.setUsername(userForm.getUsername());
                else {
                    System.out.println("\n\nINVALID USERNAME <" + userForm.getUsername() + "> \n\n");
                    model.addAttribute("error", "\nInvalid Username, alterations denied.");
                    return "editProfile.html";
                }

            } else {
                user.setUsername(user.getUsername());
            }

            userRepository.saveAndFlush(user);

            System.out.println("\nUSERNAME: " + userForm.getUsername() + " PASSWORD: " + userForm.getPasswordConfirm() + "\n");
            securityService.autoLogin(userForm.getUsername(), userForm.getPasswordConfirm(), req);

            userDetails = req.getUserPrincipal();
            user = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", user);
            model.addAttribute("user", user);



            return "viewProfile.html";

        } else {
            System.out.println("\n\nPASSWORD FOUND TO BE INCORRECT\n\n");
            model.addAttribute("error", "\nIncorrect Password, alterations denied.");
            LOGGER.warn("Unsuccessful attempt of profile edit for user <" + user.getUsername() + "> with the role of <" + userRoles + ">");
            return "editProfile.html";
        }
    }


    @PreAuthorize("#username == authentication.name")
    @GetMapping("/editPassword/{username}")
    public String changePassword(@PathVariable(value = "username") String username,
                                 @Valid @ModelAttribute("passwordUpdateForm") PasswordUpdate passwordUpdateForm,
                                 Model model, HttpServletRequest req) {

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


    @PreAuthorize("#username == authentication.name")
    @PostMapping("/editPassword/{username}")
    public String editPassword(@PathVariable("username") String username,
                               @Valid @ModelAttribute("passwordUpdateForm") PasswordUpdate passwordUpdateForm,
                               BindingResult bindingResult,
                               HttpServletResponse response, HttpServletRequest req, Model model)
            throws Exception {

        User sessionUser = null;

        Principal userDetails = req.getUserPrincipal();
        if (userDetails != null) {
            sessionUser = userRepository.findByUsername(userDetails.getName());
            model.addAttribute("sessionUser", sessionUser);
        }

        //Determine if booking as admin
        User user = null;
        if (isAdmin(sessionUser)) {
            user = userRepository.findByUsername(username);
        } else {
            user = sessionUser;
        }
        model.addAttribute("user", user);

        StringBuilder userRoles = new StringBuilder();
        for (Role role : user.getRoles()) {
            userRoles.append(role.getName());
        }

        passwordValidator.validate(passwordUpdateForm, bindingResult);

        if(bindingResult.hasErrors()){
            return "editPassword.html";
        }

        bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

        if (bCryptPasswordEncoder.matches(passwordUpdateForm.getCurrentPassword(), user.getPassword()) &&
                (passwordUpdateForm.getNewPassword().equals(passwordUpdateForm.getPasswordConfirm()))) {

            //TODO create new hashed password here

            user.setPassword(bCryptPasswordEncoder.encode(passwordUpdateForm.getNewPassword()));

            userRepository.saveAndFlush(user);
            LOGGER.info("Password successfully changed by user <" + user.getUsername() + ">");
            req.logout();
            model.addAttribute("sessionUser", user);
            model.addAttribute("user", user);

            return "viewProfile.html";

        } else {
                    model.addAttribute("error", "\nNew Password entries do not match, update denied.");
                    model.addAttribute("user", userSession.getUser());
                    LOGGER.warn("Password change rejected due to new password mismatch for user <" + user.getUsername() + "> with role of <" + userRoles + ">");

                    return "editPassword.html";
        }
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
