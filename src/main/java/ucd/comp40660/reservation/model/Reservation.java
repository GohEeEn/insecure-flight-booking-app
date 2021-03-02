package ucd.comp40660.reservation.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import lombok.ToString;
import org.w3c.dom.stylesheets.LinkStyle;

import ucd.comp40660.user.model.Guest;
import ucd.comp40660.user.model.Passenger;
import ucd.comp40660.flight.model.Flight;
import ucd.comp40660.user.model.User;

import java.util.List;

@Entity
@Table(name = "reservations")
@Data
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String email;

    @NotNull
    private Long flight_reference;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne
    private Guest guest;

    @ManyToOne
    @JsonIgnore
    private User user;

    @ManyToOne
    @JsonIgnore
    private Flight flight;

    public Reservation() {
        super();
    }

    public Reservation(Long id, String email, Long flight_reference) {
        this.id = id;
        this.email = email;
        this.flight_reference = flight_reference;
    }
}
