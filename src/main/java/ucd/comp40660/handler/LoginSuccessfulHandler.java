package ucd.comp40660.handler;

import com.auth0.jwt.JWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ucd.comp40660.service.ACUserDetails;
import ucd.comp40660.user.model.Attempts;
import ucd.comp40660.user.model.JwtToken;
import ucd.comp40660.user.repository.AttemptsRepository;
import ucd.comp40660.user.repository.JwtTokenRepository;
import ucd.comp40660.user.repository.UserRepository;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static ucd.comp40660.filter.SecurityConstants.*;

@Component
public class LoginSuccessfulHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessfulHandler.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttemptsRepository attemptsRepository;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws IOException, ServletException {

        String username = auth.getName();

        // Reset the number of attempts if the authentication is success and there was failed attempt(s) before
        Optional<Attempts> userAttempts = attemptsRepository.findAttemptsByUsername(username);

        // Reset the information for consecutive failed login attempts for certain account
        if(userAttempts.isPresent()) {
            Attempts attempts = userAttempts.get();
            attempts.setAttempts(0);
            attemptsRepository.save(attempts);
        }

        String token = JWT.create()
                .withSubject(((ACUserDetails) auth.getPrincipal()).getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(SECRET.getBytes()));

        response.addHeader(HEADER_STRING, BEARER + token);

        addCookie(token, response);

        // Add to JWT token monitoring list
        jwtTokenRepository.save(new JwtToken(userRepository.findByUsername(username), token));

        logger.info(String.format("User <%s> logins successfully, and all previous failed attempts have been reset", username));
        new DefaultRedirectStrategy().sendRedirect(request, response, "/");
    }

    private void addCookie(String token, HttpServletResponse response) {

        // create a cookie
        Cookie cookie = new Cookie(COOKIE_NAME, token);

        // expires in 30 minutes (in seconds)
        cookie.setMaxAge((int) EXPIRATION_TIME / 1000);

        // optional properties
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        // add cookie to response
        response.addCookie(cookie);
    }
}
