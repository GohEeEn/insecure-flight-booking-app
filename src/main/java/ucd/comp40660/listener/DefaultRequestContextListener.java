package ucd.comp40660.listener;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

import javax.servlet.annotation.WebListener;

/**
 * Enable the access to the HTTP request from the UserDetailsService
 * Reference : https://stackoverflow.com/questions/38202621/how-to-add-a-requestcontextlistener-with-no-xml-configuration
 */
@Configuration
@WebListener
public class DefaultRequestContextListener extends RequestContextListener {
}
