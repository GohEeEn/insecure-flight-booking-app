package ucd.comp40660.registration.model;

import javax.persistence.*;
import javax.validation.constraints.*;

import lombok.Data;

@Entity
@Table(name = "registrations")
@Data
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrationID;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotNull
    private Long phone;

    @NotBlank
    private String email;

    @NotBlank
    private String address;

    @NotBlank
    private String credit_card_details;

    @NotBlank
    private String reservation_history;

    @NotBlank
    private String upcoming_reservations;


    public Registration() {
        super();
    }

    public Registration(Long registrationID, String name, Long phone, String email, String address, String credit_card_details, String reservation_history, String upcoming_reservations) {
        this.registrationID = registrationID;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.credit_card_details = credit_card_details;
        this.reservation_history = reservation_history;
        this.upcoming_reservations = upcoming_reservations;
    }


}
