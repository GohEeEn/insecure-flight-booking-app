package ucd.comp40660.user.model;

import javax.persistence.*;
import javax.validation.constraints.*;

import lombok.Data;
import ucd.comp40660.reservation.model.Reservation;

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

    @OneToOne
    private CreditCard credit_card = new CreditCard();

    @Column
    @OneToMany(mappedBy = "guest", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @Column
    @OneToMany(mappedBy = "guest", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Passenger> passengers = new ArrayList<>();

    public Guest() {
        super();
    }

    public Guest(String name, String surname, String phone, String email, String address, List<Reservation> reservations, List<Passenger> passengers) {

        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.reservations = reservations;
        this.passengers = passengers;
    }
}

