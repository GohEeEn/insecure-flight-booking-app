package ucd.comp40660.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.core.Authentication;
import ucd.comp40660.user.model.User;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomLogoutHandler implements LogoutHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomLogoutHandler.class);

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        LOGGER.info("User logged out successfully");
    }
}
