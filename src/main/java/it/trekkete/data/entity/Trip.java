package it.trekkete.data.entity;

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

    private UUID creator;

    private Integer rating;

    private Integer maxParticipants;

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

}
