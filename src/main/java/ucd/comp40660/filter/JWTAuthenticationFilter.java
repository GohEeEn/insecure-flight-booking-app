package ucd.comp40660.filter;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import ucd.comp40660.handler.LoginFailureHandler;
import ucd.comp40660.handler.LoginSuccessfulHandler;
import ucd.comp40660.user.exception.IpAddressLockedException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private final LoginSuccessfulHandler loginSuccessfulHandler;

    @Autowired
    private final LoginFailureHandler loginFailureHandler;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager,
                                   LoginSuccessfulHandler loginSuccessfulHandler,
                                   LoginFailureHandler loginFailureHandler){

        this.authenticationManager = authenticationManager;
        this.loginSuccessfulHandler = loginSuccessfulHandler;
        this.loginFailureHandler = loginFailureHandler;
    }

    // Reference : https://stackoverflow.com/questions/34233856/spring-security-authenticationmanager-must-be-specified-for-custom-filter
    @Override
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        if (userDetailsService == null) {
            userDetailsService = FilterUtil.loadUserDetailsService(request);
        }

        String username = request.getParameter("username");
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails == null)
                unsuccessfulAuthentication(request, response, new UsernameNotFoundException("Username <" + username + "> not found"));
            else {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, request.getParameter("password"), userDetails.getAuthorities());

                return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            }
        } catch(IpAddressLockedException error) {
            unsuccessfulAuthentication(request, response, error);
        }
        return null;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication auth) throws IOException {

        try {
            loginSuccessfulHandler.onAuthenticationSuccess(request, response, auth);
        } catch (ServletException e) {
            logger.error("Something wrong with AuthenticationSuccessHandler");
            e.printStackTrace();
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        try {
            loginFailureHandler.onAuthenticationFailure(request, response, failed);
        } catch (ServletException e) {
            logger.error("Something wrong with AuthenticationFailureHandler");
            e.printStackTrace();
        }
    }
}
