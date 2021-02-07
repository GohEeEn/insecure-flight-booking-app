package ucd.comp40660.registration.controller;

import org.springframework.web.bind.annotation.RestController;
import ucd.comp40660.registration.exception.RegistrationNotFoundException;
import ucd.comp40660.registration.model.Registration;
import ucd.comp40660.registration.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class RegistrationController {

    @Autowired
    RegistrationRepository registrationRepository;

    //    Get all registrations
    @GetMapping("/registrations")
    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    //    Get a single registration by id
//    the id can be changed to any other attribute
    @GetMapping("/registrations/{id}")
    public Registration getRegistrationById(@PathVariable(value = "id") Long registrationId) throws RegistrationNotFoundException {
        return registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));
    }

    //    update registration details
    @PutMapping("/registrations/{id}")
    public Registration updateRegistration(@PathVariable(value = "id") Long registrationId, @Valid @RequestBody Registration registrationDetails) throws RegistrationNotFoundException {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

//        update the details of a registration record
        registration.setAddress(registrationDetails.getAddress());
        registration.setName(registrationDetails.getName());
        registration.setEmail(registrationDetails.getEmail());
        registration.setPhone(registrationDetails.getPhone());
        registration.setSurname(registration.getSurname());
        registration.setCredit_card_details(registrationDetails.getCredit_card_details());
        registration.setUpcoming_reservations(registrationDetails.getUpcoming_reservations());
        registration.setReservation_history(registration.getReservation_history());

        Registration updatedRegistration = registrationRepository.save(registration);

        return updatedRegistration;
    }

    //    Delete a registration record
    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<?> deleteRegistration(@PathVariable(value = "id") Long registrationID) throws RegistrationNotFoundException {
        Registration registration = registrationRepository.findById(registrationID)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationID));

        registrationRepository.delete(registration);

        return ResponseEntity.ok().build();
    }
}
