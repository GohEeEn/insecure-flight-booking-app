package ucd.comp40660.user.controller;

import ucd.comp40660.user.UserSession;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
import ucd.comp40660.user.exception.UserNotFoundException;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;


import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;



@Controller
public class UserController {

    @Autowired
    private UserSession userSession;


    @Autowired
    UserRepository userRepository;

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
        user.setUpcoming_reservations(userDetails.getUpcoming_reservations());
        user.setReservation_history(user.getReservation_history());

        User updatedUser = userRepository.save(user);

        return updatedUser;
    }

    //    Delete a registration record
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteRegistration(@PathVariable(value = "id") Long registrationID) throws UserNotFoundException {
        User user = userRepository.findById(registrationID)
                .orElseThrow(() -> new UserNotFoundException(registrationID));

        userRepository.delete(user);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/register")
    public String register(Model model, HttpServletResponse response) throws Exception{
        if(userSession.isLoginFailed()){
            model.addAttribute("error", "Unable to create account, passwords do not match");
            userSession.setLoginFailed(false);
        }
        if (userSession.getUser() != null){
            response.sendRedirect("/logout");
        }
        return "register.html";
    }

    @PostMapping("/register")
//    public User createUser(@Valid @RequestParam User user) {
//        return userRepository.save(user);
//    }
    public void createUser(String name, String surname, String username, Long phone, String address, String email, String credit_card_details,
                           String password, String passwordDuplicate  , HttpServletResponse response) throws Exception{

        if(password.equals(passwordDuplicate)){
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
            user.setReservation_history("None");
            user.setUpcoming_reservations("None");
            userRepository.save(user);
            userSession.setUser(user);
            response.sendRedirect("/");
        }
        else{
            userSession.setLoginFailed(true);
            response.sendRedirect("/register");
        }
    }



}
