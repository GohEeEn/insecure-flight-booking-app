package ucd.comp47660.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import ucd.comp47660.model.JwtToken;
import ucd.comp47660.repository.JwtTokenRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static ucd.comp47660.filter.SecurityConstants.COOKIE_NAME;

public class CustomLogoutHandler implements LogoutHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomLogoutHandler.class);

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        Cookie[] cookies = request.getCookies();
        String token = null;

        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(COOKIE_NAME)) {
                    token = cookie.getValue();
                }
            }
        }

        // Invalidate the token used in this session before logout
        // by adding it into a blacklist. Deletion of expired token
        // can be implemented if time is efficient
        JwtToken jwtToken = jwtTokenRepository.findByJwtToken(token);
        jwtToken.setLogout(true);
        jwtTokenRepository.save(jwtToken);

        LOGGER.info(String.format("User logged out successfully"));
    }
}
