package ucd.comp40660.flight.model;

public class FlightSearch {
    String departure;
    String destination;
    String date;
    String passengers;

    public FlightSearch(String departure, String destination, String date, String passengers) {
        this.departure = departure;
        this.destination = destination;
        this.date = date;
        this.passengers = passengers;
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
    
}
