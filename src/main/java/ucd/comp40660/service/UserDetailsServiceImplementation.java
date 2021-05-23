package ucd.comp40660.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ucd.comp40660.user.exception.IpAddressLockedException;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.RoleRepository;
import ucd.comp40660.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;

@Service
public class UserDetailsServiceImplementation implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        final String ip = getClientIP();

        if (loginAttemptService.isBlocked(ip)) {
            throw new IpAddressLockedException(ip);
        }

        try {
            User user = userRepository.findByUsername(username);

            if (user == null) return null;

            return new ACUserDetails(user);

        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getClientIP() {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
