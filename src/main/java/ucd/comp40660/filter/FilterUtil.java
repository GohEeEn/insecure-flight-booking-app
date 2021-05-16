package ucd.comp40660.filter;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class FilterUtil {
    static UserDetailsService loadUserDetailsService(HttpServletRequest request){
        ServletContext servletContext = request.getServletContext();
        WebApplicationContext webApplicationContext =
                WebApplicationContextUtils.getWebApplicationContext(servletContext);
        return webApplicationContext.getBean(UserDetailsService.class);
    }

}
