package ucd.comp47660.model;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import ucd.comp47660.model.User;

@Component
@SessionScope
public class UserSession {
    private User user;
    private boolean loginFailed;

    public User getUser(){
        return user;
    }

    public void setUser(User user){
        this.user = user;
    }

    public boolean isLoginFailed(){
        return loginFailed;
    }

    public void setLoginFailed(boolean loginFailed){
        this.loginFailed = loginFailed;
    }

}