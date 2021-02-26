package ucd.comp40660.reservation.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.ToString;
import ucd.comp40660.flight.model.Flight;
import ucd.comp40660.user.model.Guest;
import ucd.comp40660.user.model.User;

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
    @ManyToOne
    private Guest guest;

    @ManyToOne
    private User user;

    @ManyToOne
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
