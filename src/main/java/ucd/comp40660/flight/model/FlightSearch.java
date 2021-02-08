package ucd.comp40660.flight.model;

public class FlightSearch {
    String departure;
    String destination;
    String date;
    String passengers;
    String outboundDate;
    String returnDate;
    boolean oneWayTrip;

    public FlightSearch(String departure, String destination, String date, String passengers, String outboundDate, String returnDate, boolean oneWayTrip) {
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.passengers = passengers;
        this.outboundDate = outboundDate;
        this.returnDate = returnDate;
        this.oneWayTrip = oneWayTrip;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPassengers() {
        return passengers;
    }

    public void setPassengers(String passengers) {
        this.passengers = passengers;
    }

    public String getOutboundDate() {
        return outboundDate;
    }

    public void setOutboundDate(String outboundDate) {
        this.outboundDate = outboundDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isOneWayTrip() {
        return oneWayTrip;
    }

    public void setOneWayTrip(boolean oneWayTrip) {
        this.oneWayTrip = oneWayTrip;
    }
}
