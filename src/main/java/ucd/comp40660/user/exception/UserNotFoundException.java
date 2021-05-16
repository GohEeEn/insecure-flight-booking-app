package ucd.comp40660.user.exception;

public class UserNotFoundException extends Exception {
    private long registrationID;

    public UserNotFoundException(long registrationID) {
        super(String.format("Registration is not found with id '%s'", registrationID));
    }

    public UserNotFoundException(String username){
        super(String.format("Registration is not found with user name '%s'", username));

    }
}
