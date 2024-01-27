package it.trekkete.hikehunter.data.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "email_verification_token", catalog = "hike_hunter")
public class EmailVerificationToken {

    @Id
    private String email;

    private String token;

    private Long creationTs;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getCreationTs() {
        return creationTs;
    }

    public void setCreationTs(Long creationTs) {
        this.creationTs = creationTs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmailVerificationToken that = (EmailVerificationToken) o;

        if (!email.equals(that.email)) return false;
        return token.equals(that.token);
    }

    @Override
    public int hashCode() {
        int result = email.hashCode();
        result = 31 * result + token.hashCode();
        return result;
    }
}
