package ucd.comp40660.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import ucd.comp40660.user.exception.IpAddressLockedException;
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

import static java.lang.String.format;
import static ucd.comp40660.filter.SecurityConstants.FAILED_LOGIN_URL;
import static ucd.comp40660.filter.SecurityConstants.SPRING_SECURITY_LAST_EXCEPTION;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginFailureHandler.class);

    @Autowired
    private AttemptsRepository attemptsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private static final int ATTEMPTS_LIMIT = 3;

    private static final long LOCK_TIME_DURATION = 1200000; // 20 minutes from milliseconds

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

//        System.out.println("Handle authentication failed found");

        if(failed instanceof IpAddressLockedException) {
            logger.warn(failed.getMessage());
        } else {

            String username = request.getParameter("username");

            // Load the User object to check if this user exist
            User user = userRepository.findByUsername(username);

            // Case if this user does not exist
            if (user == null) {

                logger.warn(String.format("Username <%s> not found", username));
                failed = new UsernameNotFoundException("User <" + username + "> does not exist");

                // Prevent IP-based brute-force login attempts
                String password = request.getParameter("password");
                Authentication auth = new UsernamePasswordAuthenticationToken(username, password);
                applicationEventPublisher.publishEvent(new AuthenticationFailureBadCredentialsEvent(auth, failed));

            } else {

                // Check if the account is locked
                Optional<Attempts> userAttempts = attemptsRepository.findAttemptsByUsername(username);

                // If the exception is about using incorrect credential
                if (failed instanceof BadCredentialsException) {

                    // New fail attempt
                    if (userAttempts.isEmpty()) {
                        Attempts attempts = new Attempts(username, new Date());
                        attemptsRepository.save(attempts);
                        logger.warn(String.format("%d times of failed login attempts by <%s>", attempts.getAttempts(), username));
                        failed = new BadCredentialsException("Invalid credentials. Please try again");
                    } else {
                        Attempts attempts = userAttempts.get();
                        attempts.setAttempts(attempts.getAttempts() + 1);
                        attempts.setLastModified(new Date());
                        attemptsRepository.save(attempts);
                        logger.warn(String.format("%d times of failed login attempts by <%s>", attempts.getAttempts(), username));

                        if (attempts.getAttempts() + 1 > ATTEMPTS_LIMIT) {
                            user.setAccountNonLocked(false);
                            userRepository.save(user);
                            logger.warn(String.format("Failed login attempts by <%s> exceeds the consecutive limits, thus lock the account for 20 minutes", username));
                            failed = new LockedException("Too many invalid attempts. Your account will be locked for 20 minutes");
                        } else
                            failed = new BadCredentialsException("Invalid credentials. Please try again");
                    }

                } else if (failed instanceof LockedException) {

                    if (userAttempts.isEmpty())
                        logger.error(String.format("New failed login case by <%s> somehow lock the account", username));

                    else {

                        Attempts attempts = userAttempts.get();

                        // Handle the locked case
                        if (attempts.getAttempts() < ATTEMPTS_LIMIT) {
                            logger.error(String.format("Failed login attempts with %d times by <%s> somehow lock the account", attempts.getAttempts(), username));

                        } else {

                            // Case if the account is locked
                            if (!user.getAccountNonLocked()) {

                                // This account has exceed the allocated lock time
                                if (attempts.getLastModified().getTime() + LOCK_TIME_DURATION < new Date().getTime()) {
                                    user.setAccountNonLocked(true);
                                    userRepository.save(user);
                                    logger.warn(format("User account <%s> has been unlocked, login attempt can be done now", username));
                                    failed = new LockedException("Your account has been unlocked, please try again to login");
                                } else {
                                    logger.warn(format("User account <%s> is still locked, login attempt cannot be done now until 20 minutes later", username));
                                    failed = new LockedException("Your account is still locked, try again after 20 minutes");
                                }
                            }
                        }
                    }
                }
            }
        }

        request.getSession().setAttribute(SPRING_SECURITY_LAST_EXCEPTION, failed);
//        System.out.println("Done authentication failed handling");

        new DefaultRedirectStrategy().sendRedirect(request, response, FAILED_LOGIN_URL);
    }
}
