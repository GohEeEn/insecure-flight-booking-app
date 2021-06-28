package ucd.comp47660.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public interface SecurityService {
    String findLoggedInUsername();

    void autoLogin(String username, String password, HttpServletRequest req) throws ServletException;

    void guestLogin();
}