package ucd.comp40660.user.model;

import javax.persistence.*;
import java.util.Date;

import static ucd.comp40660.filter.SecurityConstants.EXPIRATION_TIME;

@Entity
public class JwtToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "token_id")
    private Long tokenID;

    @Column(name = "token")
    private String jwtToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(name = "has_logout", nullable = false)
    private boolean logout;

    public JwtToken(){}

    public JwtToken(User user, String token){
        this.user = user;
        this.expirationDate = new Date(new Date().getTime() + EXPIRATION_TIME);
        this.jwtToken = token;
        this.logout = false;
    }

    public String getJwtToken(){
        return jwtToken;
    }

    public void setJwtToken(String jwtToken){
        this.jwtToken = jwtToken;
    }

    public Date getExpirationDate(){
        return expirationDate;
    }

    public void setExpirationDate(Date createdDate) {
        this.expirationDate = createdDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getTokenid() {
        return tokenID;
    }

    public void setTokenid(long tokenid) {
        this.tokenID = tokenid;
    }

    public boolean isLogout() {
        return logout;
    }

    public void setLogout(boolean logout) {
        this.logout = logout;
    }
}
