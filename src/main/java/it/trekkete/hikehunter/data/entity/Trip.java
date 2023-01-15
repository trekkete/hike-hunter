package it.trekkete.hikehunter.data.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "trip", catalog = "hike_hunter")
public class Trip extends AbstractEntity {

    private String title;
    private String description;

    private Long creationTs;

    private Long startDate;
    private Long endDate;

    @Type(type = "uuid-char")
    private UUID creator;

    private Integer rating;

    private Integer maxParticipants;

    private String equipment;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreationTs() {
        return creationTs;
    }

    public void setCreationTs(Long creationTs) {
        this.creationTs = creationTs;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public UUID getCreator() {
        return creator;
    }

    public void setCreator(UUID creator) {
        this.creator = creator;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public static String formatRating(Integer rating) {
        return switch (rating) {
            case 1 -> "Molto facile";
            case 2 -> "Facile";
            case 3 -> "Media";
            case 4 -> "Difficile";
            case 5 -> "Molto difficile";
            default -> "Difficoltà ignota";
        };
    }

    public Long getXp() {
        return switch (rating) {
            case 1 -> 40L;
            case 2 -> 50L;
            case 3 -> 70L;
            case 4 -> 100L;
            case 5 -> 150L;
            default -> 0L;
        };
    }

}
