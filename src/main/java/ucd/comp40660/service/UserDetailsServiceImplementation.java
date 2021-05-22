package ucd.comp40660.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.UserRepository;

@Service
public class UserDetailsServiceImplementation implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        User user = userRepository.findByUsername(username);

        if(user == null) {
            throw new UsernameNotFoundException("Username <" + username + "> not found");
        }

        return new ACUserDetails(user);
    }
}
