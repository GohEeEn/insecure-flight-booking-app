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

    public User(String name, String surname, String username, String role,  Long phone, String email, String address, String credit_card_details, String password, String reservation_history, String upcoming_reservations) {
        this.registrationID = registrationID;
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


    public Long getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(Long registrationID) {
        this.registrationID = registrationID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getPhone() {
        return phone;
    }

    public void setPhone(Long phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCredit_card_details() {
        return credit_card_details;
    }

    public void setCredit_card_details(String credit_card_details) {
        this.credit_card_details = credit_card_details;
    }

    public String getReservation_history() {
        return reservation_history;
    }

    public void setReservation_history(String reservation_history) {
        this.reservation_history = reservation_history;
    }

    public String getUpcoming_reservations() {
        return upcoming_reservations;
    }

    public void setUpcoming_reservations(String upcoming_reservations) {
        this.upcoming_reservations = upcoming_reservations;
    }
}
