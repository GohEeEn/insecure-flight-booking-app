package ucd.comp40660;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ucd.comp40660.filter.*;
import ucd.comp40660.handler.CustomLogoutHandler;
import ucd.comp40660.handler.LoginFailureHandler;
import ucd.comp40660.handler.LoginSuccessfulHandler;
import ucd.comp40660.service.UserDetailsServiceImplementation;

import java.util.Arrays;

import static ucd.comp40660.filter.SecurityConstants.*;

@Configuration
@EnableWebSecurity
@EnableEncryptableProperties
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Qualifier("userDetailsServiceImplementation")
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private final LoginSuccessfulHandler loginSuccessfulHandler;

    @Autowired
    private final LoginFailureHandler loginFailureHandler;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return authProvider;
    }

    @Bean
    public CustomLogoutHandler customLogoutHandler() {
        return new CustomLogoutHandler();
    }

    public WebSecurityConfig(UserDetailsServiceImplementation userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder,
                             LoginSuccessfulHandler loginSuccessfulHandler, LoginFailureHandler loginFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.loginFailureHandler = loginFailureHandler;
        this.loginSuccessfulHandler = loginSuccessfulHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Enable h2 access via the h2-console
        http.authorizeRequests().antMatchers("/h2-console/**").permitAll()
                .and().csrf().ignoringAntMatchers("/h2-console/**")
                .and().headers().frameOptions().sameOrigin();


        // Enable Cross Origin RequestS (cors) and disable Spring Boot CSRF protection
        // CSRF protection is automatically enabled by Spring Security to create a stateful session, while we are using stateless session here
        // thus it has to be disabled here
        http.cors().and().csrf().disable()
                .requiresChannel().anyRequest().requiresSecure()        // Require HTTPS Requests
                .and()
                .authorizeRequests()
                .antMatchers("/error", "/resources/**", "/img/**", "/css/**", "/js/**", LOGIN_URL, "/register", "/").permitAll()
                .antMatchers("/user","/user/delete/", "/processMemberPayment").access("hasAnyAuthority('ADMIN','MEMBER')")
                .antMatchers("/editProfile", "/editPassword", "/viewCreditCards").access("hasAuthority('MEMBER')")
                .antMatchers("/admin", "/adminRegister", "/users", "/flights", "/reservations", "/deleteReservation", "/guestRegister", "/adminRegister",
                        "/registerFlight", "/updateFlight", "/deleteFlight").access("hasAuthority('ADMIN')")
                .anyRequest().authenticated()   // Authenticate all requests, with exception URL regexes mentioned above
                .and()
                .formLogin()
                .defaultSuccessUrl("/", true)     // The landing page after a successful login
                .successHandler(loginSuccessfulHandler)
                .failureUrl(FAILED_LOGIN_URL)                        // Landing page after an unsuccessful login
                .failureHandler(loginFailureHandler)
                .loginPage(LOGIN_URL).permitAll()                        // Specify URL for login
                .and()
                .logout()
                .logoutUrl("/logout")           // Specify URL for logout
                .addLogoutHandler(customLogoutHandler())
                .clearAuthentication(true)          // Invalidate the authentication after logout
                .deleteCookies(COOKIE_NAME)     // Delete the cookie containing the JWT after logout
                .permitAll()
                .and()
                // Filtering by intercepting incoming requests and execute predefined methods
                .addFilter(new JWTAuthenticationFilter(authenticationManager(), loginSuccessfulHandler, loginFailureHandler))    // filter support authentication
                .addFilter(new JWTAuthorisationFilter(authenticationManager()))     // filter support authorization
                // Enforce stateless sessions : this disables session creation 0on Spring Security
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .invalidSessionUrl(LOGIN_URL + "?expired");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        configuration.setAllowedOrigins(Arrays.asList("localhost"));
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

}
