package ucd.comp47660.exception;

public class FlightNotFoundException extends Exception{
    private long flightID;

    public FlightNotFoundException(long flightID){
        super(String.format("Flight is not found with id '%s'", flightID));
    }
}
