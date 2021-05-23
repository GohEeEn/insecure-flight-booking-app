package ucd.comp40660.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ucd.comp40660.reservation.model.Reservation;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ucd.comp40660.filter.RegexConstants.NAME_REGEX;
import static ucd.comp40660.filter.RegexConstants.PASSWORD_REGEX;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "email", "phone"})})
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrationID;

    @NotBlank(message = "First Name field must not be empty.")
    @Size(min = 2, max = 100, message = "Invalid name")
    @Pattern(regexp = NAME_REGEX)
    private String name;

    @NotBlank(message = "Surname field must not be empty.")
    @Size(min = 2, max = 100, message = "Invalid surname")
    @Pattern(regexp = NAME_REGEX)
    private String surname;

    @Column(unique = true)
    @NotBlank(message = "Username field must not be empty.")
    @Size(min = 4, max = 32, message = "Invalid username")
    private String username;

    @NotBlank(message = "Password field must not be empty.")
    @Pattern(regexp = PASSWORD_REGEX)
    private String password;

    @Column(unique = true)
    @NotNull(message = "Phone number field must not be empty.")
    private String phone;

    @Column(unique = true)
    @Email(message = "Valid e-mail address required.")
    @NotBlank(message = "E-mail field must not be empty.")
    private String email;


    @NotBlank(message = "Address field must not be empty.")
    private String address;

    @Transient
    private String passwordConfirm;

    @OneToMany(mappedBy = "user")
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    @ToString.Exclude
    private List<CreditCard> credit_cards = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    @ToString.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    @Column
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Passenger> passengers = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    @ToString.Exclude
    @JoinTable(name="user_roles",
            joinColumns = @JoinColumn(name="user_id", referencedColumnName="registrationID"),
            inverseJoinColumns = @JoinColumn(name="role_id", referencedColumnName="id")
    )
    private Set<Role> roles = new HashSet<>();


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    @Column(name = "account_non_locked")
    private boolean accountNonLocked;

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public boolean getAccountNonLocked() {
        return this.accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public List<CreditCard> getCredit_cards(){
        return this.credit_cards;
    }

    public User() {
        super();
    }

    public User(String name, String surname, String username, String phone, String email, String address, String password, String passwordConfirm, List<Reservation> reservations) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.reservations = reservations;
        this.accountNonLocked = true;
    }
}

