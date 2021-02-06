package ucd.comp40660.flight.exception;

public class FlightNotFoundException extends Exception{
    private long flightID;

    public FlightNotFoundException(long flightID){
        super(String.format("Flight is not found with id '%s'", flightID));
    }
}
