package ucd.comp40660.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Service that is defined to prevent IP-based brute-force attack on login authentication
 */
@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

    private static final int MAX_ATTEMPT = 3;

    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        super();
        attemptsCache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES) // Locked time for 20 minutes
                                    .build(new CacheLoader<String, Integer>() {
            @Override
            public Integer load(final String key) {
                return 0;
            }
        });
    }

    /**
     * Method to reset the counter for successful login from certain IP address.
     * @param ip_addr
     */
    public void loginSucceeded(final String ip_addr) {

        try {
            logger.info(String.format("Login success and thus previous %d failed login attempt(s) by <%s> has been removed", attemptsCache.get(ip_addr), ip_addr));
            attemptsCache.invalidate(ip_addr);
        } catch (final ExecutionException e) {
            logger.error(String.format("Execution exception happen when resetting the failed login attempts from <%s> detected", ip_addr));
        }
    }

    /**
     * Method to register every login failure attempt from certain IP address
     * @param ip_addr
     */
    public void loginFailed(final String ip_addr) {
        int attempts = 0;
        try {
            attempts = attemptsCache.get(ip_addr);
            logger.warn(String.format("%d login attempt failed by <%s> detected", attempts + 1, ip_addr));
        } catch (final ExecutionException e) {
            attempts = 0;
            logger.error(String.format("Execution exception caused by the %d failed login attempt(s) from <%s> detected", attempts + 1, ip_addr));
        }
        attempts++;
        attemptsCache.put(ip_addr, attempts);
    }

    /**
     * check if the IP address is blocked due to failed login attempts exceed the define limit
     * @param ip_addr
     * @return
     */
    public boolean isBlocked(final String ip_addr) {
        try {
            boolean blocked = attemptsCache.get(ip_addr) >= MAX_ATTEMPT;
            if(blocked) {
                logger.warn(String.format("Failed login attempts by <%s> has exceed the limit, thus it is be block for 20 minutes", ip_addr));
            }
            return blocked;
        } catch (final ExecutionException e) {
            return false;
        }
    }
}
