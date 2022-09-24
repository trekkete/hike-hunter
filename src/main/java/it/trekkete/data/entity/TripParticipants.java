package it.trekkete.data.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "trip_participants", catalog = "hike_hunter")
@IdClass(TripParticipantsKey.class)
public class TripParticipants {

    @Id
    @Type(type = "uuid-char")
    private UUID trip;

    @Id
    @Type(type = "uuid-char")
    private UUID user;

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
}
