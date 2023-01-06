package it.trekkete.data.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "trip_location", catalog = "hike_hunter")
@IdClass(TripLocationKey.class)
public class TripLocation {

    @Id
    @Type(type = "uuid-char")
    private UUID trip;

    @Id
    private String location;

    @Column(name = "idx")
    private Integer index;

    public UUID getTrip() {
        return trip;
    }

    public void setTrip(UUID trip) {
        this.trip = trip;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Column(nullable = false)
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}