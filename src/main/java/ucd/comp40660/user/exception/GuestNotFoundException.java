package ucd.comp40660.user.exception;

public class GuestNotFoundException extends Exception{
    private long guestId;

    public GuestNotFoundException(long guestId){
        super(String.format("Guest is not found with id '%s'", guestId));
    }
}

