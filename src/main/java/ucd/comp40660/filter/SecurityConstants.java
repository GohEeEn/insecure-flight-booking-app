package ucd.comp40660.filter;

public final class SecurityConstants {

    private SecurityConstants() {
        throw new IllegalStateException("Utility class");
    }

    // JWT token defaults
    public static final String SECRET = "4u7x!A%D*F-JaNdRgUkXp2s5v8y/B?E(H+KbPeShVmYq3t6w9z$C&F)J@NcQfTjWnZr4u7x!A%D*G-KaPdSgUkXp2s5v8y/B?E(H+MbQeThWmYq3t6w9z$C&F)J@NcRfUjXn2r4u7x!A%D*G-KaPdSgVkYp3s6v8y/B?E(H+MbQeThWmZq4t7w!z$C&F)J@NcRfUjXn2r5u8x/A?D*G-KaPdSgVkYp3s6v9y$B&E)H+MbQeThWmZq4t7w!z%C*F-";
    public static final long EXPIRATION_TIME = 1_800_000; // 30 minutes in milliseconds
    public static final String BEARER = "Bearer ";
    public static final String COOKIE_NAME = "JWT";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/register";
    public static final String LOGIN_URL = "/login";
    public static final String FAILED_LOGIN_URL = "/login?error=true";
    public static final String SPRING_SECURITY_LAST_EXCEPTION = "SPRING_SECURITY_LAST_EXCEPTION";
}
