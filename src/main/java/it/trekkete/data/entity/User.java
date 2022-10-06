package it.trekkete.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.trekkete.data.Role;
import java.util.Set;
import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "user", catalog = "hike_hunter")
public class User extends AbstractEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Column(nullable = false)
    private Long creationTs;

    private Long lastLoginTs;

    @Column(columnDefinition = "text default null")
    private String extendedData;

    @Column(nullable = false)
    private Long xp;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Long getCreationTs() {
        return creationTs;
    }

    public void setCreationTs(Long creationTs) {
        this.creationTs = creationTs;
    }

    public Long getLastLoginTs() {
        return lastLoginTs;
    }

    public void setLastLoginTs(Long lastLoginTs) {
        this.lastLoginTs = lastLoginTs;
    }

    public String getExtendedData() {
        return extendedData;
    }

    public void setExtendedData(String extendedData) {
        this.extendedData = extendedData;
    }

    public Long getXp() {
        return xp;
    }

    public void setXp(Long xp) {
        this.xp = xp;
    }

    public Integer getLevel() {
        return (int) Math.ceil(Math.sqrt(xp * 2)) + 1;
    }

    public Integer getMaxXp() {

        Integer level = getLevel();

        return (level * (level + 1)) / 2 * 100;
    }
}
