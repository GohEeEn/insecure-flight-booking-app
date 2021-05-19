package ucd.comp40660.filter;

public final class RegexConstants {

    private RegexConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String NAME_REGEX = "([A-Za-z]*).{2,32}";

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
}
