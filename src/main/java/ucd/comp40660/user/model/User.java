package ucd.comp40660.user.model;

import javax.persistence.*;
import javax.validation.constraints.*;

import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrationID;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotBlank
    private String role;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

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


    public User() {
        super();
    }

//    public User(Long registrationID, String name, Long phone, String email, String address, String credit_card_details, String reservation_history, String upcoming_reservations) {
//        this.registrationID = registrationID;
//        this.name = name;
//        this.phone = phone;
//        this.email = email;
//        this.address = address;
//        this.credit_card_details = credit_card_details;
//        this.reservation_history = reservation_history;
//        this.upcoming_reservations = upcoming_reservations;
//    }

    public User(String name, String surname, String username, String role, Long phone, String email, String address, String credit_card_details, String password, String reservation_history, String upcoming_reservations) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.credit_card_details = credit_card_details;
        this.password = password;
        this.reservation_history = reservation_history;
        this.upcoming_reservations = upcoming_reservations;
    }
}

