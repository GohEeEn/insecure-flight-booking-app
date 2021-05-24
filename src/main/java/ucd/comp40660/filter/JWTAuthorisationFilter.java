package ucd.comp40660.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import ucd.comp40660.service.JwtTokenService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static ucd.comp40660.filter.SecurityConstants.*;


@Component
public class JWTAuthorisationFilter extends BasicAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTAuthorisationFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenService jwtTokenService;

    public JWTAuthorisationFilter(AuthenticationManager authenticationManager){
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException{

        Cookie[] cookies = request.getCookies();
        String token = null;

        Cookie jwtCookie = null;

        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(COOKIE_NAME)) {
                    jwtCookie = cookie;
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Pass the request to the next filter in the chain if no JWT token is associated
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request, token);

        // Delete the invalid cookie from the response and the web browser
        if(authentication == null) {
            jwtCookie.setPath("/");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setMaxAge(0);
            response.addCookie(jwtCookie);
        }

        // Add the authentication information to the context of the request
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Pass the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }

    /**
     * Verify the authentication credentials associated with the token
     * @param request
     * @param token
     * @return
     */
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, String token) {

        if(token != null){

            String username;

            try {
                username = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                        .build()
                        .verify(token.replace(BEARER, ""))    // Verify the token signature
                        .getSubject();                                   // Retrieve the username
            } catch(TokenExpiredException error) {
                log.warn("This request uses an expired JWT token, it will be deleted");
                return null;
            } catch(Exception e) {
                log.warn("This request uses an invalid value for JWT token, it will be deleted");
                return null;
            }

            if(jwtTokenService == null) {
                jwtTokenService = FilterUtil.loadJwtTokenService(request);
            }

            if(jwtTokenService.isValidToken(token)){

                if(userDetailsService == null){
                    userDetailsService = FilterUtil.loadUserDetailsService(request);
                }
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

            } else {
                log.warn(String.format("Invalid JWT token is used for authorisation : %s", token));
            }

            return null;
        }

        log.error(String.format("Null JWT token was received during token authentication"));
        return null;
    }
}

