package ucd.comp40660.filter;

import com.auth0.jwt.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ucd.comp40660.service.ACUserDetails;
import ucd.comp40660.user.model.Attempts;
import ucd.comp40660.user.repository.AttemptsRepository;
import ucd.comp40660.user.repository.UserRepository;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static ucd.comp40660.filter.SecurityConstants.*;


public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AttemptsRepository attemptsRepository;

    @Autowired
    private UserRepository userRepository;

    private static final int ATTEMPTS_LIMIT = 3;

    private static final long LOCK_TIME_DURATION = 1200000; // 20 minutes from milliseconds

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        if (userDetailsService == null) {
            userDetailsService = FilterUtil.loadUserDetailsService(request);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getParameter("username"));
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, request.getParameter("password"), userDetails.getAuthorities());

        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication auth) throws IOException {

        // Reset the number of attempts if the authentication is success and there was failed attempt(s) before
        Optional<Attempts> userAttempts = attemptsRepository.findAttemptsByUsername(auth.getName());

        if(userAttempts.isPresent()) {
            Attempts attempts = userAttempts.get();
            attempts.setAttempts(0);
            attemptsRepository.save(attempts);
        }

        String token = JWT.create()
                .withSubject(((ACUserDetails) auth.getPrincipal()).getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(SECRET.getBytes()));
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + token);

        addCookie(token, response);

        new DefaultRedirectStrategy().sendRedirect(request, response, "/");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        try {
            System.out.println("Detect authentication failure successfully");
            getFailureHandler().onAuthenticationFailure(request, response, failed);
        } catch (ServletException e) {
            e.printStackTrace();
        }

//        String username = request.getParameter("username");
//
//        ACUserDetails userDetails = (ACUserDetails) userDetailsService.loadUserByUsername(username);
//        System.out.println(userDetails.getUsername());
//
//        // Load the User object to check if this user exist
//        User user = userRepository.findByUsername("vincent");
////        System.out.println(username);
//
//        // Case if this user does not exist
//        if(user == null) throw new UsernameNotFoundException("User <" + username + "> does not exist");
//
//        // Check if the account is locked
//        Optional<Attempts> userAttempts = attemptsRepository.findAttemptsByUsername(username);
//
//        // If the exception is about using incorrect credential
//        if(failed instanceof BadCredentialsException) {
//
//            // New fail attempt
//            if (userAttempts.isEmpty()) {
//                Attempts attempts = new Attempts(username, new Date());
//                attemptsRepository.save(attempts);
//            } else {
//                Attempts attempts = userAttempts.get();
//                attempts.setAttempts(attempts.getAttempts() + 1);
//                attempts.setLastModified(new Date());
//                attemptsRepository.save(attempts);
//                logger.warn("The " + attempts.getAttempts() + " times of failed login attempts by <" + username + ">");
//
//                if (attempts.getAttempts() + 1 > ATTEMPTS_LIMIT) {
//                    user.setAccountNonLocked(false);
//                    userRepository.save(user);
//                    logger.warn("Failed login attempts by <" + username + "> exceeds the consecutive limits, thus lock the account for 20 minutes");
//                    failed = new LockedException("Too many invalid attempts. Your account will be locked for 20 minutes");
//                }
//            }
//
//        } else if(failed instanceof LockedException) {
//
//            if(userAttempts.isEmpty())
//                logger.error("New failed login case by <" + username + "> somehow lock the account");
//
//            else {
//
//                Attempts attempts = userAttempts.get();
//
//                // Handle the locked case
//                if(attempts.getAttempts() < ATTEMPTS_LIMIT) {
//                    logger.error("Failed login attempts with " + attempts.getAttempts() + " times by <" + username + "> somehow lock the account");
//                } else {
//
//                    // Case if the account is locked
//                    if(!user.getAccountNonLocked()) {
//
//                        // This account has exceed the allocated lock time
//                        if(attempts.getLastModified().getTime() + LOCK_TIME_DURATION < new Date().getTime()) {
//                            user.setAccountNonLocked(true);
//                            userRepository.save(user);
//                            logger.warn("User account <" + username + "> has been unlocked, login attempt can be done now");
//                        } else {
//                            logger.warn("User account <" + username + "> is still locked, login attempt cannot be done now until 20 minutes later");
//                        }
//                    }
//                }
//            }
//        }
//
////        super.unsuccessfulAuthentication(request, response, failed);
//        new DefaultRedirectStrategy().sendRedirect(request, response, "/login?error=true");
    }

    private void addCookie(String token, HttpServletResponse response) {

        // create a cookie
        Cookie cookie = new Cookie(COOKIE_NAME, token);

        // expires in 30 minutes
        cookie.setMaxAge(30 * 60);

        // optional properties
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        // add cookie to response
        response.addCookie(cookie);
    }
}
