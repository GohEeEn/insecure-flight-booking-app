package ucd.comp40660.user.model;

import javax.persistence.*;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

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
    private User user;

    @OneToOne
    private Guest guest;

    public CreditCard() { super(); }

    public CreditCard(String cardholder_name, String card_number, String type, int expiration_month, int expiration_year, String security_code){
        this.cardholder_name = cardholder_name;
        this.card_number = card_number;
        this.type = type;
        this.expiration_month = expiration_month;
        this.expiration_year = expiration_year;
        this.security_code = security_code;
    }

}
