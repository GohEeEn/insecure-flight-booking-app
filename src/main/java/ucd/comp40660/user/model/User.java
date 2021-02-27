package ucd.comp40660.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ucd.comp40660.reservation.model.Reservation;

import javax.persistence.*;
import javax.validation.constraints.*;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "email", "phone"})})
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrationID;

    @NotBlank(message = "First Name field must not be empty.")
    private String name;

    @NotBlank(message = "Surname field must not be empty.")
    private String surname;

    @NotBlank(message = "Role improperly initialised.")
    private String role;

    @Column(unique = true)
    @NotBlank(message = "Username field must not be empty.")
    private String username;

    @NotBlank(message = "Password field must not be empty.")
    private String password;

    @Column(unique = true)
    @NotNull(message = "Password Duplicate field must not be empty.")
    private String phone;

    @Column(unique = true)
    @Email(message = "Valid e-mail address required.")
    @NotBlank(message = "E-mail field must not be empty.")
    private String email;

    @NotBlank(message = "Address field must not be empty.")
    private String address;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    @JsonIgnore
    private List<CreditCard> credit_cards = new ArrayList<>();

    @Column
    @OneToMany(mappedBy = "user")
    private List<Reservation> reservations = new ArrayList<>();

    public User() {
        super();
    }

    public User(String name, String surname, String username, String role, String phone, String email, String address, String password, List<Reservation> reservations) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.password = password;
        this.reservations = reservations;
    }
}

