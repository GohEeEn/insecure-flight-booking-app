package ucd.comp40660.handler;

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

    private static final long LOCK_TIME_DURATION = 1200000; // 20 minutes in milliseconds

    private static final String USERNAME_PARAM = "username";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        String username = request.getParameter(USERNAME_PARAM);

        // Load the User object to check if this user exist
        User user = userRepository.findByUsername(username);

        // Failed login attempt with unknown username from certain IP address
        if(user == null)
            failed = ipBasedFailureHandler(username, request.getParameter("password"), failed);

        // Failed login attempt with certain registered username
        else
            failed = accountBasedFailureHandler(user, failed);

        request.getSession().setAttribute(SPRING_SECURITY_LAST_EXCEPTION, failed);

        new DefaultRedirectStrategy().sendRedirect(request, response, FAILED_LOGIN_URL);
    }

    /**
     * Prevent IP-based brute-force login attempts, ie. login attempt with incorrect username from certain IP address
     * @param username
     * @param password
     * @param failed
     * @return
     */
    private AuthenticationException ipBasedFailureHandler(String username, String password, AuthenticationException failed) {

        if(failed instanceof IpAddressLockedException) {
            logger.warn(String.format("The IP address sending this request has been locked"));

        } else if(failed instanceof UsernameNotFoundException) {
            logger.warn(String.format("Username <%s> not found", username));
            createAuthFailureBadCredentialsEvent(username, password, failed);
        }

        return failed;
    }

    private void createAuthFailureBadCredentialsEvent(String username, String password, AuthenticationException failed) {
        Authentication auth = new UsernamePasswordAuthenticationToken(username, password);
        applicationEventPublisher.publishEvent(new AuthenticationFailureBadCredentialsEvent(auth, failed));
    }

    /**
     * Prevent account-based brute-force login attempts, ie. login attempt with correct username but wrong password
     * @param user
     * @param failed
     * @return
     */
    private AuthenticationException accountBasedFailureHandler(User user, AuthenticationException failed) {

        String username = user.getUsername();

        // Check if the account is locked
        Optional<Attempts> userAttempts = attemptsRepository.findAttemptsByUsername(username);

        // If the exception is about using incorrect credential
        if (failed instanceof BadCredentialsException) {
            return accountBasedFailureWithinLimit(user, userAttempts, failed);
        } else if (failed instanceof LockedException) {
            return accountBasedFailureExceedLimit(user, userAttempts, failed);
        } else {
            logger.error(failed.getMessage());
        }

        return failed;
    }

    /**
     * Handling of account-based login failure attempt when it is within the attempt limit.
     * Tasks including increase the failed login attempts, and lock the account with it is more than the attempt limit
     * @param user
     * @param userAttempts
     * @param failed
     * @return
     */
    private AuthenticationException accountBasedFailureWithinLimit(User user, Optional<Attempts> userAttempts, AuthenticationException failed) {

        String username = user.getUsername();

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

        return failed;
    }

    /**
     * Handling of account-based login failure attempt when it exceeds the attempt limit.
     * It unlocks the account when the correct password is given after the lock time is over,
     * but requires another correct login attempt in order to login successfully.
     * Also, it denied the login attempt when it is still within the lock time
     *
     * @param user
     * @param userAttempts
     * @param failed
     * @return
     */
    private AuthenticationException accountBasedFailureExceedLimit(User user, Optional<Attempts> userAttempts, AuthenticationException failed) {

        String username = user.getUsername();

        if (userAttempts.isEmpty())
            logger.error(String.format("New failed login case by <%s> somehow lock the account", username));

        else {

            Attempts attempts = userAttempts.get();

            if (attempts.getAttempts() < ATTEMPTS_LIMIT) {
                logger.error(String.format("Failed login attempts with %d times by <%s> somehow lock the account", attempts.getAttempts(), username));

            } else {

                // Case if the account is locked
                if (!user.getAccountNonLocked()) {

                    // This account has exceed the allocated lock time
                    if (attempts.getLastModified().getTime() + LOCK_TIME_DURATION < new Date().getTime()) {
                        user.setAccountNonLocked(true);
                        userRepository.save(user);
                        logger.warn(String.format("User account <%s> has been unlocked with correct credential, login attempt can be done now", username));
                        failed = new LockedException("Your account has been unlocked, please try again to login");
                    } else {
                        logger.warn(String.format("User account <%s> is still locked, login attempt cannot be done now until 20 minutes later", username));
                        failed = new LockedException("Your account is still locked, try again after 20 minutes");
                    }
                }
            }
        }

        return failed;
    }
}
