package it.trekkete.hikehunter.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.trekkete.hikehunter.data.Role;
import java.util.Set;
import javax.persistence.*;

@Entity
@Table(name = "user", catalog = "hike_hunter")
public class User extends AbstractEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Column(nullable = false)
    private Long creationTs;

    private Long lastLoginTs;

    @Column(columnDefinition = "text DEFAULT NULL")
    private String extendedData;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Integer getLevel(Long xp) {
        for (int i = 0; i < LEVELS.length; i++) {
            if (xp < LEVELS[i]) {
                return i + 1;
            }
        }

        return LEVELS.length;
    }

    public String getLevelLabel(Integer level) {
        return LABELS[level - 1];
    }

    public Integer getMaxXp(Integer level) {
        return LEVELS[level - 1];
    }

    private static final int[] LEVELS = new int[]{100,300,600,1000,1500,2100,2800,3600,4500,5500,6600,7800,9100,10500,12000,13600,15300,17100,19000,21000,23100,25300,27600,30000,32500,35100,37800,40600,43500,46500};

    private static final String[] LABELS = {
            "Spirito Selvaggio",
            "Esploratore della Natura",
            "Vagabondo Collinare",
            "Camminatore del Sentiero",
            "Vedutista delle Meraviglie",
            "Navigatore Sereno",
            "Viandante Avvolto dal Vento",
            "Maestro dell'Arte dei Percorsi",
            "Eremita del Bosco",
            "Navigatore delle Alture",
            "Sogno su Vette e Valli",
            "Guida dell'Anima Errante",
            "Custode dei Picchi",
            "Pioniere dei Sentieri Nascosti",
            "Guardiano delle Prospettive",
            "Orientista delle Stelle",
            "Scalatore delle Cime Celesti",
            "Curatore della Terra",
            "Errante dei Luoghi Incontaminati",
            "Ambasciatore della Natura",
            "Studioso degli Ecosistemi",
            "Mago della Sopravvivenza",
            "Navigatore Notturno",
            "Condottiero dei Compagni",
            "Vigilante dell'Altopiano",
            "Alchimista Ambientale",
            "Leggendario Naturalista",
            "Poeta delle Avventure Ecologiche",
            "Messaggero del Regno Naturale",
            "Icona del Mondo Naturale"
    };
}
