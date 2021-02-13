package ucd.comp40660.flight.model;

public class FlightSearch {
    String departure;
    String destinationInput;
    String outboundDate;
    int passengers;

    public FlightSearch() {
    }

    public FlightSearch(String departure, String destinationInput, String outboundDate, int passengers) {
        this.departure = departure;
        this.destinationInput = destinationInput;
        this.outboundDate = outboundDate;
        this.passengers = passengers;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestinationInput() {
        return destinationInput;
    }

    public void setDestinationInput(String destinationInput) {
        this.destinationInput = destinationInput;
    }

    public String getOutboundDate() {
        return outboundDate;
    }

    public void setOutboundDate(String outboundDate) {
        this.outboundDate = outboundDate;
    }

    public int getPassengers() {
        return passengers;
    }

    public void setPassengers(int passengers) {
        this.passengers = passengers;
    }
}
