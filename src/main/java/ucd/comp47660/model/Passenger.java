package ucd.comp47660.model;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "passengers")
@Data
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    String name;

    @NotBlank
    String surname;

    @NotBlank
    String phone;

    @NotBlank
    String email;

    @NotBlank
    String address;

    @ToString.Exclude
    @ManyToOne(cascade = CascadeType.ALL)
    private Guest guest;

    @ToString.Exclude
    @ManyToOne(cascade = CascadeType.ALL)
    private User user;

    @ToString.Exclude
    @ManyToOne(cascade = CascadeType.ALL)
    private Reservation reservation;

    public Passenger(String name, String surname, String phone, String email, String address) {

        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public Passenger() { super(); }

}

