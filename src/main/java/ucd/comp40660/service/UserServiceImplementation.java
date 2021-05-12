package ucd.comp40660.service;

import ucd.comp40660.user.model.ConfirmationToken;
import ucd.comp40660.user.model.Role;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.ConfirmationTokenRepository;
import ucd.comp40660.user.repository.UserRepository;
import ucd.comp40660.user.repository.RoleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImplementation implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("USER");
        Set<Role> roleSet = new HashSet<Role>();
        roleSet.add(userRole);
        user.setRoles(roleSet);
        userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void save(ConfirmationToken token) {

        // Save it
        confirmationTokenRepository.save(token);
    }

    @Override
    public User isValidToken(String confirmationToken) {
        ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);

        if (token != null) {
            if(!isTokenExpired(token.getCreatedDate()) && !token.isUsed()) {
                User user = userRepository.findByEmail(token.getUser().getEmail());
                token.setUsed(true);
                confirmationTokenRepository.save(token);


                return user;
            }
        }
        return null;
    }

    @Override
    public void savePassword(String email, String password) {
        User tokenUser = userRepository.findByEmail(email);
        tokenUser.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(tokenUser);
    }



    private boolean isTokenExpired(Date createdDate){
        Date now = new Date();

        long milliseconds1 = createdDate.getTime();
        long milliseconds2 = now.getTime();
        long diffMinutes = (milliseconds2 - milliseconds1)/ (60 * 1000);
        if(diffMinutes <= 20 ) return false;


        return true;
    }

}
