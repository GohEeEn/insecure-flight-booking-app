package ucd.comp40660.user.model;

import javax.persistence.*;
import javax.validation.constraints.*;

import lombok.Data;

@Entity
@Table(name = "guest", uniqueConstraints={@UniqueConstraint(columnNames = {"email", "phone"})})
@Data
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guestID;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @Column(unique=true)
    @NotNull
    private String phone;

    @Column(unique=true)
    @NotBlank
    private String email;

    @NotBlank
    private String address;

    @NotBlank
    private String credit_card_details;

    public Guest() {
        super();
    }

//    public User(Long registrationID, String name, Long phone, String email, String address, String credit_card_details, String reservation_history, String upcoming_reservations) {
//    }

    public Guest(String name, String surname, String phone, String email, String address, String credit_card_details) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.credit_card_details = credit_card_details;
    }
}

