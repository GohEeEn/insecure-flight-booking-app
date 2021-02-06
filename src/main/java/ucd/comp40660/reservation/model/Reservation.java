package ucd.comp40660.reservation.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;



@Entity
@Table(name = "reservations")
@Data
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationID;

    @NotBlank
    private String email;

    @NotNull
    private Long flight_reference;


    public Reservation(){
        super();
    }


    public Reservation(Long reservationID, String email, Long flight_reference){
        this.reservationID = reservationID;
        this.email = email;
        this.flight_reference = flight_reference;
    }
}
