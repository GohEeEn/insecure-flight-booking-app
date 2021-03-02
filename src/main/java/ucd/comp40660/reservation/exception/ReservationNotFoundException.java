package ucd.comp40660.reservation.exception;

public class ReservationNotFoundException extends Exception {

    public ReservationNotFoundException() {
        super(String.format("Reservations not found"));
    }

    public ReservationNotFoundException(long reservationID) {
        super(String.format("Reservation is not found with id '%s'", reservationID));
    }
}
