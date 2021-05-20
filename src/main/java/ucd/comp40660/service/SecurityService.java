package ucd.comp40660.service;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface SecurityService {
    String findLoggedInUsername();

    void autoLogin(String username, String password);

    void guestLogin();
}