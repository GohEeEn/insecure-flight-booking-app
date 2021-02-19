package ucd.comp40660.user.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import ucd.comp40660.user.UserSession;
import ucd.comp40660.user.repository.UserRepository;
import ucd.comp40660.user.model.User;
import ucd.comp40660.user.controller.UserController;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;



@Controller
public class AuthenticationController{

    @Autowired
    private UserSession userSession;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String login(Model model){
//        model.addAttribute("title", "Library Login");
        if(userSession.isLoginFailed()){
            model.addAttribute("error", "Username and Password combination incorrect.");
            userSession.setLoginFailed(false);
        }
        return "login.html";
    }

    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws Exception {
        userSession.setUser(null);
        response.sendRedirect("/");
    }

    @PostMapping("/login")
    public void doLogin(String username, String password, HttpServletResponse response) throws Exception{
        Optional<User> user = userRepository.findAllByUsernameAndPassword(username, password);
        if(user.isPresent()){
            userSession.setUser(user.get());
            response.sendRedirect("/");
        }
        else {
            userSession.setLoginFailed(true);
            response.sendRedirect("/login");
        }
    }

}