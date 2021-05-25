package ucd.comp40660.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ucd.comp40660.user.model.JwtToken;
import ucd.comp40660.user.model.Role;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.repository.JwtTokenRepository;
import ucd.comp40660.user.repository.RoleRepository;
import ucd.comp40660.user.repository.UserRepository;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static ucd.comp40660.filter.SecurityConstants.EXPIRATION_TIME;

@Service
public class UserServiceImplementation implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void save(User user) {
        bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setAccountNonLocked(true);
        Role userRole = roleRepository.findByName("MEMBER");
        Set<Role> roleSet = new HashSet<Role>();
        roleSet.add(userRole);
        user.setRoles(roleSet);
        userRepository.saveAndFlush(user);
    }

    @Override
    public void adminSave(User user) {
        bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setAccountNonLocked(true);
        Role userRole = roleRepository.findByName("ADMIN");
        Set<Role> roleSet = new HashSet<Role>();
        roleSet.add(userRole);
        user.setRoles(roleSet);
        userRepository.save(user);
    }

    @Override
    public void guestSave(User user){
        bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setAccountNonLocked(true);
        Role userRole = roleRepository.findByName("GUEST");
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
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    public void save(JwtToken token) {
        jwtTokenRepository.save(token);
    }

    @Override
    public User isValidToken(String jwtToken) {
        JwtToken token = jwtTokenRepository.findByJwtToken(jwtToken);

        if (token != null) {
            if(!isTokenExpired(token.getExpirationDate()) && !token.isLogout()) {
                User user = userRepository.findByEmail(token.getUser().getEmail());
                token.setLogout(true);
                jwtTokenRepository.save(token);

                return user;
            }
        }
        return null;
    }

    @Override
    public void savePassword(String email, String password) {
        User tokenUser = userRepository.findByEmail(email);
        bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
        tokenUser.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(tokenUser);
    }

    private boolean isTokenExpired(Date createdDate){

        long milliseconds1 = createdDate.getTime();
        long milliseconds2 = new Date().getTime();
        long diffMS = (milliseconds2 - milliseconds1);
        return diffMS > EXPIRATION_TIME;
    }

}
