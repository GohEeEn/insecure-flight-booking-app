package ucd.comp47660.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ucd.comp47660.model.Passenger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ucd.comp47660.filter.RegexConstants.*;

@Component
public class PassengerValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return Passenger.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        Passenger passenger = (Passenger) o;

        if(!isValid(passenger.getName(), FLIGHT_NAME_REGEX))
            errors.rejectValue("name", "InvalidFirstName");

        if(!isValid(passenger.getSurname(), FLIGHT_NAME_REGEX))
            errors.rejectValue("surname", "InvalidLastName");

        if(!isValid(String.valueOf(passenger.getEmail()), EMAIL_REGEX))
            errors.rejectValue("email", "InvalidEmail");

        if(!isValid(String.valueOf(passenger.getAddress()), ADDRESS_REGEX))
            errors.rejectValue("address", "InvalidAddress");

        if(!isValid(passenger.getPhone(), PHONE_REGEX))
            errors.rejectValue("phone", "InvalidPhone");
    }

    private boolean isValid(String toValidate, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(toValidate);
        return matcher.matches();
    }

}
