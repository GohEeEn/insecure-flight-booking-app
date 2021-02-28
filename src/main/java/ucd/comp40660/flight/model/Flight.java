package ucd.comp40660.flight.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import ucd.comp40660.reservation.model.Reservation;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flights")
@Data
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightID;

    @NotBlank
    private String source;

    @NotBlank
    private String destination;

    @NotBlank
    private String departure_date_time;

    @NotBlank
    private String arrival_date_time;

    @Column
    @OneToMany(mappedBy = "flight")
    private List<Reservation> reservations = new ArrayList<>();

    public Flight() {
        super();
    }


    public Flight(Long flightID, String from, String destination, String departureDateTime, String arrivalDateTime) {
        this.flightID = flightID;
        this.source= from;
        this.destination= destination;
        this.departure_date_time= departureDateTime;
        this.arrival_date_time= arrivalDateTime;
    }
}
