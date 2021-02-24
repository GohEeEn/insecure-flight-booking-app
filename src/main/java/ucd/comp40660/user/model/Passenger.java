package ucd.comp40660.user.model;

public class Passenger {
    String nanme;
    String surname;
    String phone;
    String email;
    String address;

    public Passenger(String nanme, String surname, String phone, String email, String address) {
        this.nanme = nanme;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public Passenger() {
    }

    public String getNanme() {
        return nanme;
    }

    public void setNanme(String nanme) {
        this.nanme = nanme;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
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
}

