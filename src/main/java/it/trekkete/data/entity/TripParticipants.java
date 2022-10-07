package it.trekkete.data.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "trip_participants", catalog = "hike_hunter")
@IdClass(TripParticipantsKey.class)
public class TripParticipants {

    public enum Status {
        OK,
        KO
    }

    @Id
    @Type(type = "uuid-char")
    private UUID trip;

    @Id
    @Type(type = "uuid-char")
    private UUID user;

    private Status status;

    public UUID getTrip() {
        return trip;
    }

    public void setTrip(UUID trip) {
        this.trip = trip;
    }

    public UUID getUser() {
        return user;
    }

    public void setUser(UUID user) {
        this.user = user;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
