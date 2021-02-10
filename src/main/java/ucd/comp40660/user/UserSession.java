package ucd.comp40660.user;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import ucd.comp40660.user.model.User;

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