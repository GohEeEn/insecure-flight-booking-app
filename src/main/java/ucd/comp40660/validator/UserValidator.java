package ucd.comp40660.validator;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ucd.comp40660.service.UserService;
import ucd.comp40660.user.model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ucd.comp40660.filter.RegexConstants.NAME_REGEX;
import static ucd.comp40660.filter.RegexConstants.PASSWORD_REGEX;

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

        if ((!user.getPasswordConfirm().equals(user.getPassword())) ||
            (!isValid(user.getPassword(), PASSWORD_REGEX)) ||
            (user.getUsername().length() < 4 || user.getUsername().length() > 32) ||
            (!isUserValid(user.getUsername())) ||
            (userService.findByUsername(user.getUsername()) != null)
        )
            errors.rejectValue("passwordConfirm", "Diff.userForm.passwordConfirm");
    }

    public void validatePassword(String password, Errors errors) {
        if (!isValid(password, PASSWORD_REGEX))
            errors.rejectValue("passwordConfirm", "Diff.userForm.newPassword");
    }

    private boolean isEmailValid(String email){
        return EmailValidator.getInstance().isValid(email);
    }

    private boolean isUserValid(String username){
        return username.matches("^[a-z0-9_-]{6,32}$");
    }

    private boolean isValid(String toValidate, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(toValidate);
        return matcher.matches();
    }
}
