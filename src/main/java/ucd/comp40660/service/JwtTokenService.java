package ucd.comp40660.service;

import com.auth0.jwt.JWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ucd.comp40660.user.repository.JwtTokenRepository;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static ucd.comp40660.filter.SecurityConstants.EXPIRATION_TIME;
import static ucd.comp40660.filter.SecurityConstants.SECRET;

@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    /**
     * Retrieve username from jwt token
     * @param token
     * @return
     */
    public String getUsernameFromToken(String token) {
        return jwtTokenRepository.findByJwtToken(token).getUser().getUsername();
    }

    /**
     * Retrieve expiration date from jwt token
     * @param token
     * @return
     */
    public Date getExpirationDateFromToken(String token) {
        return jwtTokenRepository.findByJwtToken(token).getExpirationDate();
    }

    /**
     * Check if the token has expired
     * @param token
     * @return
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Check if the token has been used and the user has logout
     * @param token
     * @return
     */
    private Boolean isUserLogout(String token) {
        return jwtTokenRepository.findByJwtToken(token).isLogout();
    }

    /**
     * Generate token for user
     * @param authentication
     * @return
     */
    public String generateToken(Authentication authentication) {

        return JWT.create()
            .withSubject(((ACUserDetails) authentication.getPrincipal()).getUsername())
            .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .sign(HMAC512(SECRET.getBytes()));
    }

    /**
     * Validate token by checking if the token belongs to certain user
     * or the user using this token has not logout yet
     *
     * @param token
     * @return
     */
    public Boolean isValidToken(String token) {

        final boolean with_username = getUsernameFromToken(token) != null;
        final boolean has_logout = isUserLogout(token);

        if(!with_username) log.warn("Username not found in the given JWT token");
        if(has_logout) log.warn("This JWT token has been used by a logout user");

        return with_username && !has_logout;
    }
}
