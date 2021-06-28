package ucd.comp47660.exception;

public class GuestNotFoundException extends Exception{
    private long guestId;

    public GuestNotFoundException(long guestId){
        super(String.format("Guest is not found with id '%s'", guestId));
    }
}

