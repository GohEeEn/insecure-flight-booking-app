package ucd.comp40660.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.exception.IpAddressLockedException;
import ucd.comp40660.user.model.Role;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static ucd.comp40660.filter.SecurityConstants.LOGIN_URL;
import static ucd.comp40660.filter.SecurityConstants.SPRING_SECURITY_LAST_EXCEPTION;


@Controller
public class AuthenticationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private UserSession userSession;

    @GetMapping(LOGIN_URL)
    public String login(Model model, HttpServletRequest request,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "expired", required = false) String expired) {

        if (error != null) {
            model.addAttribute("error", getErrorMessage(request, SPRING_SECURITY_LAST_EXCEPTION));
        }

        if (expired != null) {
            model.addAttribute("msg", "You've been logged out due to cookie expired.");
        }

        if (logout != null) {
            model.addAttribute("msg", "You've been logged out successfully.");
        }

        return "login.html";
    }

    /**
     * Assign the customized error message to be returned to front-end depending on the exception found
     * @param request
     * @param key       SPRING_SECURITY_LAST_EXCEPTION, a constant that shows the latest exception
     *                  caught in the authentication process
     * @return associated error message in the exception
     */
    private String getErrorMessage(HttpServletRequest request, String key){

        Exception exception = (Exception) request.getSession().getAttribute(key);

        String error = "";

        if(exception instanceof IpAddressLockedException) {
            error = "You are locked from further login attempts for 20 minutes. Please come back later.";
        } else if (exception instanceof BadCredentialsException || exception instanceof LockedException) {
            error = exception.getMessage();
        }else{
            error = "Invalid username and password!";
        }

        return error;
    }

    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws Exception {

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userSession.getUser().getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("User <" + userSession.getUser().getUsername() + "> with the role of <" + userRoles + "> logged out successfully");
        userSession.setUser(null);
        response.sendRedirect("/");
    }

    @PostMapping(LOGIN_URL)
    public String doLogin(String username, String password, HttpServletResponse response) throws Exception {

        StringBuilder userRoles = new StringBuilder();
        for (Role role : userSession.getUser().getRoles()) {
            userRoles.append(role.getName());
        }

        LOGGER.info("User <" + username + "> with the role of <" + userRoles + "> logged in successfully");
        return "index.html";
    }

    @GetMapping("/guestLogin")
    public void guestLogin(HttpServletRequest request, Model model) throws ServletException {

        request.login("testguest", "password1234");
    }

}