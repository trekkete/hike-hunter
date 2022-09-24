package it.trekkete.data.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TripLocationKey implements Serializable {

    private UUID trip;
    private UUID location;
    private int index;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TripLocationKey that = (TripLocationKey) o;

        if (index != that.index) return false;
        if (!Objects.equals(trip, that.trip)) return false;
        return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        int result = trip != null ? trip.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + index;
        return result;
    }
}
