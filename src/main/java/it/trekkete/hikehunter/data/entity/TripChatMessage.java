package it.trekkete.hikehunter.data.entity;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "trip_chat_message", catalog = "hike_hunter")
public class TripChatMessage extends AbstractEntity{


    @Type(type = "uuid-char")
    UUID trip;

    @Type(type = "uuid-char")
    UUID user;

    Long ts;

    String content;

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

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
