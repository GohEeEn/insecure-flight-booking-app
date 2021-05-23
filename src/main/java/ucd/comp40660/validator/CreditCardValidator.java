package ucd.comp40660.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ucd.comp40660.user.model.CreditCard;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ucd.comp40660.filter.RegexConstants.*;
@Component
public class CreditCardValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return CreditCard.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        CreditCard creditCard = (CreditCard) o;

        if(!isValid(creditCard.getCardholder_name(), FLIGHT_NAME_REGEX))
            errors.rejectValue("cardholder_name", "InvalidCreditCardName");

        if(!isValid(creditCard.getCard_number(), SIMPLE_CREDIT_CARD_REGEX))
            errors.rejectValue("card_number", "InvalidCreditCardNumber");

        if(!isValid(String.valueOf(creditCard.getExpiration_month()), MONTH_REGEX))
            errors.rejectValue("expiration_month", "InvalidExpirationMonth");

        if(!isValid(String.valueOf(creditCard.getExpiration_year()), YEAR_REGEX))
            errors.rejectValue("expiration_year", "InvalidExpirationYear");

        if(!isValid(creditCard.getSecurity_code(), CVV_REGEX))
            errors.rejectValue("security_code", "InvalidCVVCode");
    }

    private boolean isValid(String toValidate, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(toValidate);
        return matcher.matches();
    }

}
