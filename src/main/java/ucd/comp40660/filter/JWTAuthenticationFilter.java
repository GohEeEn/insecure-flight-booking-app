package ucd.comp40660.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

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

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        if (userDetailsService == null) {
            userDetailsService = FilterUtil.loadUserDetailsService(request);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getParameter("username"));
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, request.getParameter("password"), userDetails.getAuthorities());

        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication auth) throws IOException {

        try {
//            System.out.println(loginSuccessfulHandler.getClass());
            loginSuccessfulHandler.onAuthenticationSuccess(request, response, auth);
        } catch (ServletException e) {
            logger.error("Something wrong with AuthenticationSuccessHandler");
            e.printStackTrace();
        }

//        System.out.println("Successful authentication done");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        try {
//            System.out.println("Detect authentication failure successfully");
            loginFailureHandler.onAuthenticationFailure(request, response, failed);
        } catch (ServletException e) {
            logger.error("Something wrong with AuthenticationFailureHandler");
            e.printStackTrace();
        }
    }
}
