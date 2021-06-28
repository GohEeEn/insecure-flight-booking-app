package ucd.comp47660.service;

import ucd.comp47660.model.JwtToken;
import ucd.comp47660.model.User;

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
