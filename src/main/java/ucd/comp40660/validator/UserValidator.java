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

@Component
public class UserValidator implements Validator {

    private final String NAME_REGEX = "([A-Za-z]*).{2,32}";

    /**
     * (?=.*[a-z])   The string must contain at least 1 lowercase alphabetical character
     * (?=.*[A-Z])	The string must contain at least 1 uppercase alphabetical character
     * (?=.*[0-9])	The string must contain at least 1 numeric character
     * (?=.*[!@#$%^&*])	The string must contain at least one special character, but we are escaping reserved RegEx characters to avoid conflict
     * (?=.{8,32})	The string must be eight characters or longer up to 32 characters
     */
    private final String PASSWORD_REGEX = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{8,32})";

    /**
     * Reference : https://owasp.org/www-community/OWASP_Validation_Regex_Repository
     */
    private final String CREDIT_CARD_REGEX = "((4\\d{3})|(5[1-5]\\d{2})|(6011)|(7\\d{3}))-?\\d{4}-?\\d{4}-?\\d{4}|3[4,7]\\d{13}";

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
            errors.rejectValue("firstname", "InvalidFirstName");

        if(!isValid(user.getSurname(), NAME_REGEX))
            errors.rejectValue("lastname","InvalidLastName");

        if(!isEmailValid(user.getEmail()))
            errors.rejectValue("email", "InvalidEmail");

        if ((user.getUsername().length() < 4 || user.getUsername().length() > 32) ||
            (!isUserValid(user.getUsername())) ||
            (userService.findByEmail(user.getEmail())!=null) ||
            (userService.findByUsername(user.getUsername()) != null) ||
            (!user.getPasswordConfirm().equals(user.getPassword())) ||
            (!isValid(user.getPassword(), PASSWORD_REGEX))
        )
            errors.rejectValue("passwordConfirm", "Diff.userForm.passwordConfirm");
    }

    public void validatePassword(String password, Errors errors) {
        if (!isValid(password, PASSWORD_REGEX))
            errors.rejectValue("passwordConfirm", "Diff.userForm.passwordConfirm");
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
