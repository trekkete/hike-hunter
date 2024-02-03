package it.trekkete.hikehunter.data.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "password_recovery_token", catalog = "hike_hunter")
@IdClass(PasswordRecoveryToken.PasswordRecoveryTokenKey.class)
public class PasswordRecoveryToken {

    public static class PasswordRecoveryTokenKey implements Serializable {

        private UUID user;

        private UUID token;

        public PasswordRecoveryTokenKey() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PasswordRecoveryTokenKey that = (PasswordRecoveryTokenKey) o;

            if (!Objects.equals(user, that.user)) return false;
            return Objects.equals(token, that.token);
        }

        @Override
        public int hashCode() {
            int result = user != null ? user.hashCode() : 0;
            result = 31 * result + (token != null ? token.hashCode() : 0);
            return result;
        }
    }

    @Id
    @Type(type = "uuid-char")
    private UUID user;

    @Id
    @Type(type = "uuid-char")
    private UUID token;

    private Long creationTs;

    public UUID getUser() {
        return user;
    }

    public void setUser(UUID user) {
        this.user = user;
    }

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public Long getCreationTs() {
        return creationTs;
    }

    public void setCreationTs(Long creationTs) {
        this.creationTs = creationTs;
    }
}
