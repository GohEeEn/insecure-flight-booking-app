package ucd.comp40660.user.model;

import javax.persistence.*;
import javax.validation.constraints.*;

import jdk.dynalink.linker.LinkerServices;
import lombok.Data;
import lombok.ToString;
import ucd.comp40660.reservation.model.Reservation;
import ucd.comp40660.user.model.CreditCard;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guests", uniqueConstraints = {@UniqueConstraint(columnNames = {"email", "phone"})})
@Data
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @Column(unique = true)
    @NotNull
    private String phone;

    @Column(unique = true)
    @NotBlank
    private String email;

    @NotBlank
    private String address;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    private CreditCard credit_card = new CreditCard();


    @NotBlank
    private String credit_card_details;

    @Column
    @OneToMany(mappedBy = "guest", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @Column
    @OneToMany(mappedBy = "guest", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Passenger> passengers = new ArrayList<>();

    public Guest() {
        super();
    }


//    public User(Long registrationID, String name, Long phone, String email, String address, String credit_card_details, String reservation_history, String upcoming_reservations) {
//    }

    public Guest(String name, String surname, String phone, String email, String address, String credit_card_details, List<Reservation> reservations, List<Passenger> passengers) {

        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.credit_card_details = credit_card_details;
        this.reservations = reservations;
        this.passengers = passengers;
    }
}

