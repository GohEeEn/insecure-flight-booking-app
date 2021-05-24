package ucd.comp40660.user.exception;

import org.springframework.security.authentication.LockedException;

public class IpAddressLockedException extends LockedException {

    public IpAddressLockedException() {
        super("This IP address has been locked. Please come back after 20 minutes");
    }

    public IpAddressLockedException(String ip_address) {
        super("<" + ip_address + "> has been locked for 20 minutes due to exceeding the limit of failed login attempts");
    }
}
