package ucd.comp40660.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import ucd.comp40660.user.model.Attempts;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.AttemptsRepository;
import ucd.comp40660.user.repository.UserRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFailureHandler.class);

    @Autowired
    private AttemptsRepository attemptsRepository;

    @Autowired
    private UserRepository userRepository;

    private static final int ATTEMPTS_LIMIT = 3;

    private static final long LOCK_TIME_DURATION = 1200000; // 20 minutes from milliseconds

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        System.out.println("Handle authentication failed found");
        String username = request.getParameter("username");

        // Load the User object to check if this user exist
        User user = userRepository.findByUsername("vincent");

        // Case if this user does not exist
        if(user == null) throw new UsernameNotFoundException("User <" + username + "> does not exist");

        // Check if the account is locked
        Optional<Attempts> userAttempts = attemptsRepository.findAttemptsByUsername(username);

        // If the exception is about using incorrect credential
        if(failed instanceof BadCredentialsException) {

            // New fail attempt
            if (userAttempts.isEmpty()) {
                Attempts attempts = new Attempts(username, new Date());
                attemptsRepository.save(attempts);
            } else {
                Attempts attempts = userAttempts.get();
                attempts.setAttempts(attempts.getAttempts() + 1);
                attempts.setLastModified(new Date());
                attemptsRepository.save(attempts);
                logger.warn("The " + attempts.getAttempts() + " times of failed login attempts by <" + username + ">");

                if (attempts.getAttempts() + 1 > ATTEMPTS_LIMIT) {
                    user.setAccountNonLocked(false);
                    userRepository.save(user);
                    logger.warn("Failed login attempts by <" + username + "> exceeds the consecutive limits, thus lock the account for 20 minutes");
                    failed = new LockedException("Too many invalid attempts. Your account will be locked for 20 minutes");
                }
            }

        } else if(failed instanceof LockedException) {

            if(userAttempts.isEmpty())
                logger.error("New failed login case by <" + username + "> somehow lock the account");

            else {

                Attempts attempts = userAttempts.get();

                // Handle the locked case
                if(attempts.getAttempts() < ATTEMPTS_LIMIT) {
                    logger.error("Failed login attempts with " + attempts.getAttempts() + " times by <" + username + "> somehow lock the account");
                } else {

                    // Case if the account is locked
                    if(!user.getAccountNonLocked()) {

                        // This account has exceed the allocated lock time
                        if(attempts.getLastModified().getTime() + LOCK_TIME_DURATION < new Date().getTime()) {
                            user.setAccountNonLocked(true);
                            userRepository.save(user);
                            logger.warn("User account <" + username + "> has been unlocked, login attempt can be done now");
                        } else {
                            logger.warn("User account <" + username + "> is still locked, login attempt cannot be done now until 20 minutes later");
                        }
                    }
                }
            }
        }

//        super.unsuccessfulAuthentication(request, response, failed);
        new DefaultRedirectStrategy().sendRedirect(request, response, "/login?error=true");
    }
}
