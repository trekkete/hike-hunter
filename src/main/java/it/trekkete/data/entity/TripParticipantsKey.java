package it.trekkete.data.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TripParticipantsKey implements Serializable {

    private UUID trip;

    private UUID user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TripParticipantsKey that = (TripParticipantsKey) o;

        if (!Objects.equals(trip, that.trip)) return false;
        return Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        int result = trip != null ? trip.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
