package ucd.comp40660.flight.model;

import lombok.Data;
import ucd.comp40660.reservation.model.Reservation;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
    @Temporal(TemporalType.TIMESTAMP)
    private Date departure_date_time;

    @Temporal(TemporalType.TIMESTAMP)
    private Date cancelLimitTime;

    @NotBlank
    @Temporal(TemporalType.TIMESTAMP)
    private Date arrivalDateTime;

    @Column
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    public Flight() {
        super();
    }


    public Flight(String from, String destination, Date departureDateTime, Date arrivalDateTime) {
//        this.flightID = flightID;
        this.source= from;
        this.destination= destination;
        this.departure_date_time= departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.cancelLimitTime = setCancelTime();
    }

    public Timestamp setCancelTime(){
        Timestamp ct = new Timestamp(0);
        ct.setTime(this.departure_date_time.getTime() - (3600 * 1000 * 24));
        return ct;
    }
}
