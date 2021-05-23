package ucd.comp40660.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ucd.comp40660.user.model.PasswordUpdate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ucd.comp40660.filter.RegexConstants.*;

@Component
public class PasswordValidator implements Validator{

    @Override
    public boolean supports(Class<?> aClass){
        return PasswordUpdate.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        PasswordUpdate passwordUpdate = (PasswordUpdate) o;

        if(!(passwordUpdate.getNewPassword().equals(passwordUpdate.getPasswordConfirm()))){
            errors.rejectValue("passwordConfirm", "Diff.passwordForm.passwords");
        }

        if(!isValid(passwordUpdate.getNewPassword(), PASSWORD_REGEX)){
            errors.rejectValue("newPassword", "Diff.userForm.newPassword");
        }
    }

    private boolean isValid(String toValidate, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(toValidate);
        return matcher.matches();
    }


}
