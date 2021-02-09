package ucd.comp40660.flight.model;

public class FlightSearch {
    String departure;
    String destinationInput;
    String outboundDate;
    String passengers;

    public FlightSearch() {
    }

    public FlightSearch(String departure, String destinationInput, String outboundDate, String passengers) {
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

    public String getPassengers() {
        return passengers;
    }

    public void setPassengers(String passengers) {
        this.passengers = passengers;
    }
}
