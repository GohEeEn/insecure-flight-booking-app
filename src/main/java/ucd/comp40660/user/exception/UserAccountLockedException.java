package ucd.comp40660.user.exception;

import org.springframework.security.authentication.LockedException;

public class UserAccountLockedException extends LockedException {

    public UserAccountLockedException() {
        super("This account has been locked. Please come back after 20 minutes");
    }

    public UserAccountLockedException(String username) {
        super("User <" + username + "> has been locked for 20 minutes due to consecutive incorrect password");
    }
}
