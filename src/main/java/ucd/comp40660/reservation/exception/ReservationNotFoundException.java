package ucd.comp40660.reservation.exception;

public class ReservationNotFoundException extends Exception{

    private long reservationID;

    public ReservationNotFoundException(long reservationID){
        super(String.format("Reservation is not found with id '%s'", reservationID));
    }
}
