package it.trekkete.hikehunter.data.service;

import it.trekkete.hikehunter.data.entity.Trip;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    Trip findTripById(UUID id);

    List<Trip> findAllByOrderByCreationTsDesc();

    List<Trip> findAllByRating(Integer rating);

    List<Trip> findAllByRatingLessThanEqual(Integer rating);

    List<Trip> findAllByTitleContaining(String title, Sort sort);

    @Query(value = "SELECT T.id AS trip, (COALESCE(T.max_participants, 100) - participants.number) AS available FROM trip as T JOIN (SELECT trip, COUNT(user) AS number FROM trip_participants GROUP BY trip) AS participants ON T.id = participants.trip WHERE LOWER(T.title) LIKE %:title% GROUP BY id ORDER BY available DESC", nativeQuery = true)
    List<Object[]> findAllByTitleContainingSortByAvailabilityImpl(@Param("title") String title);

    default List<Trip> findAllByTitleContainingSortByAvailability(String title) {

        List<Trip> trips = new ArrayList<>();
        List<Object[]> counts = findAllByTitleContainingSortByAvailabilityImpl(title.toLowerCase());

        if (counts == null)
            return null;

        counts.forEach(entry -> trips.add(findTripById(UUID.fromString((String) entry[0]))));

        return trips;
    }

}
