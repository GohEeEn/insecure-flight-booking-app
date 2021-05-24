package ucd.comp40660.service;

import ucd.comp40660.user.model.JwtToken;
import ucd.comp40660.user.model.User;

public interface UserService {

    void save(User user);

    void adminSave(User user);

    void guestSave(User user);

    User findByUsername(String username);

    User findByEmail(String email);

    User findByPhone(String phone);

    void save(JwtToken confirmationToken);

    User isValidToken(String confirmationToken);

    void savePassword(String email, String password);

}
