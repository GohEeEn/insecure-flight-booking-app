package ucd.comp47660.validator;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ucd.comp47660.service.UserService;
import ucd.comp47660.model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ucd.comp47660.filter.RegexConstants.*;

@Component
public class UserValidator implements Validator {

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> aClass){
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        User user = (User) o;

        if(!isValid(user.getName(), NAME_REGEX))
            errors.rejectValue("name", "InvalidFirstName");

        if(!isValid(user.getSurname(), NAME_REGEX))
            errors.rejectValue("surname","InvalidLastName");

        if(!isEmailValid(user.getEmail()) || (userService.findByEmail(user.getEmail())!=null))
            errors.rejectValue("email", "InvalidEmail");

        if(userService.findByPhone(user.getPhone()) != null)
            errors.rejectValue("phone", "InvalidPhone");

        if(!isValid(user.getAddress(), ADDRESS_REGEX)){
            errors.rejectValue("address", "InvalidAddress");
        }

        if ((!user.getPasswordConfirm().equals(user.getPassword())) ||
            (!isValid(user.getPassword(), PASSWORD_REGEX)) ||
            (user.getUsername().length() < 4 || user.getUsername().length() > 32) ||
            (!isUserValid(user.getUsername()))
        )
            errors.rejectValue("passwordConfirm", "Diff.userForm.passwordConfirm");

        if((userService.findByUsername(user.getUsername()) != null) ||
                (!isValid(user.getUsername(), USERNAME_REGEX))){
            errors.rejectValue("username", "InvalidUsername");
        }
    }

    public boolean isPasswordValid(String password) {
        return isValid(password, PASSWORD_REGEX);
    }

    public boolean isEmailValid(String email){
        return EmailValidator.getInstance().isValid(email);
    }

    public boolean isUserValid(String username){
        return username.matches(USERNAME_REGEX);
    }

    private boolean isValid(String toValidate, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(toValidate);
        return matcher.matches();
    }
}
