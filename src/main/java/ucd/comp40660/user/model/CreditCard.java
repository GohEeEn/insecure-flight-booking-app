package ucd.comp40660.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ucd.comp40660.reservation.model.Reservation;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

import static ucd.comp40660.filter.RegexConstants.CREDIT_CARD_REGEX;

@Entity
@Table(name="credit_cards", uniqueConstraints = {@UniqueConstraint(columnNames = {"card_number"})})
@Data
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String cardholder_name;

    @NotBlank
    @Pattern(regexp = CREDIT_CARD_REGEX, message = "Invalid card number")
    private String card_number;

    @NotBlank
    private String type;

    @NotBlank
    private int expiration_month;

    @NotBlank
    private int expiration_year;

    @NotBlank
    private String security_code;

    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "credit_card")
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    @ToString.Exclude
    private List<Reservation> reservations = new ArrayList<>();

    public CreditCard() { super(); }

    public CreditCard(String cardholder_name, String card_number, String type, int expiration_month, int expiration_year, String security_code){
        this.cardholder_name = cardholder_name;
        this.card_number = card_number;
        this.type = type;
        this.expiration_month = expiration_month;
        this.expiration_year = expiration_year;
        this.security_code = security_code;
    }

    public String maskedCardNumber(){
        String maskedDetails = "Ends with ";
        maskedDetails += card_number.substring(card_number.length()-4);
        return maskedDetails;
    }

}
