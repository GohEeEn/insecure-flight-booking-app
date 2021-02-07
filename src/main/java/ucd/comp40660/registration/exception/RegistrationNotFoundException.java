package ucd.comp40660.registration.exception;

public class RegistrationNotFoundException extends Exception {
    private long registrationID;

    public RegistrationNotFoundException(long registrationID) {
        super(String.format("Registration is not found with id '%s'", registrationID));
    }
}
