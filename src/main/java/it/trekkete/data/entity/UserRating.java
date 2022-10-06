package it.trekkete.data.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "user_rating", catalog = "hike_hunter")
@IdClass(UserRating.UserRatingKey.class)
public class UserRating{

    public static class UserRatingKey implements Serializable {
        private UUID from;
        private UUID about;
        private UUID trip;
    }

    @Id
    @Type(type = "uuid-char")
    private UUID from;

    @Id
    @Type(type = "uuid-char")
    private UUID about;

    @Id
    @Type(type = "uuid-char")
    private UUID trip;

    private Integer preparation;
    private Integer skill;
    private Integer sociability;

    public UUID getTrip() {
        return trip;
    }

    public void setTrip(UUID trip) {
        this.trip = trip;
    }

    public UUID getFrom() {
        return from;
    }

    public void setFrom(UUID from) {
        this.from = from;
    }

    public UUID getAbout() {
        return about;
    }

    public void setAbout(UUID about) {
        this.about = about;
    }

    public Integer getPreparation() {
        return preparation;
    }

    public void setPreparation(Integer preparation) {
        this.preparation = preparation;
    }

    public Integer getSkill() {
        return skill;
    }

    public void setSkill(Integer skill) {
        this.skill = skill;
    }

    public Integer getSociability() {
        return sociability;
    }

    public void setSociability(Integer sociability) {
        this.sociability = sociability;
    }
}
