package ucd.comp40660.filter;

public final class RegexConstants {

    private RegexConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String NAME_REGEX = "^[A-Za-z]{2,32}$";

    /**
     * (?=.*[a-z])   The string must contain at least 1 lowercase alphabetical character
     * (?=.*[A-Z])	The string must contain at least 1 uppercase alphabetical character
     * (?=.*[0-9])	The string must contain at least 1 numeric character
     * (?=.*[!@#$%^&*])	The string must contain at least one special character, but we are escaping reserved RegEx characters to avoid conflict
     * (?=.{8,32})	The string must be eight characters or longer up to 32 characters
     */
    public static final String PASSWORD_REGEX = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{8,32})";

    /**
     * Reference : https://owasp.org/www-community/OWASP_Validation_Regex_Repository
     */
    public static final String CREDIT_CARD_REGEX = "((4\\d{3})|(5[1-5]\\d{2})|(6011)|(7\\d{3}))-?\\d{4}-?\\d{4}-?\\d{4}|3[4,7]\\d{13}";

    //Credit Card regexes
    public static final String SIMPLE_CREDIT_CARD_REGEX = "^[0-9]{16}$";
    public static final String MONTH_REGEX = "^[1-9]$|^[1][0-2]$";
    public static final String YEAR_REGEX = "^[2][0][2][1-9]$";
    public static final String CVV_REGEX = "^[0-9]{3}$";

    //Guest && Passenger additional regexes for Flight Booking

    // Sourcr : https://owasp.org/www-community/OWASP_Validation_Regex_Repository -> PERSON NAME
    public static final String FLIGHT_NAME_REGEX = "^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z ]*)*$";
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{4,20}$";
    public static final String ADDRESS_REGEX = "^[a-zA-Z0-9]+(([',. -][a-zA-Z0-9 ])?[a-zA-Z0-9]*)*$";
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String PHONE_REGEX = "^[0-9]{3}[-][0-9]{3}[-][0-9]{4}$";
}
