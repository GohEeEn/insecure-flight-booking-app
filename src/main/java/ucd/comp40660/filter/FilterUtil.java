package ucd.comp40660.filter;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ucd.comp40660.service.JwtTokenService;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Used to support the autowiring issue that couldn't been done in JWTAuthorisation class
 * Reference : https://stackoverflow.com/questions/32494398/unable-to-autowire-the-service-inside-my-authentication-filter-in-spring/32495757
 */
public class FilterUtil {

    private FilterUtil() {}

    static UserDetailsService loadUserDetailsService(HttpServletRequest request){
        ServletContext servletContext = request.getServletContext();
        WebApplicationContext webApplicationContext =
                WebApplicationContextUtils.getWebApplicationContext(servletContext);
        return webApplicationContext.getBean(UserDetailsService.class);
    }

    static JwtTokenService loadJwtTokenService(HttpServletRequest request){
        ServletContext servletContext = request.getServletContext();
        WebApplicationContext webApplicationContext =
                WebApplicationContextUtils.getWebApplicationContext(servletContext);
        return webApplicationContext.getBean(JwtTokenService.class);
    }
}
