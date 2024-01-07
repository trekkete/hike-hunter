package it.trekkete.hikehunter.data.service;

import it.trekkete.hikehunter.data.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    Trip findTripById(UUID id);

    Page<Trip> findAllByStartDateGreaterThanEqualOrderByStartDateAsc(Long startDate, Pageable pageable);
    Page<Trip> findAllByStartDateGreaterThanEqualOrderByCreationTs(Long startDate, Pageable pageable);

    List<Trip> findAllByRating(Integer rating);

    List<Trip> findAllByStartDateGreaterThanEqualAndRatingLessThanEqual(Long startDate, Integer rating);

    List<Trip> findAllByStartDateGreaterThanEqualAndTitleContainingIgnoreCase(Long startDate, String title, Sort sort);

    @Query(value = "SELECT T.id AS trip, (COALESCE(T.max_participants, 100) - participants.number) AS available FROM trip as T JOIN (SELECT trip, COUNT(user) AS number FROM trip_participants GROUP BY trip) AS participants ON T.id = participants.trip WHERE T.startDate >= :start AND LOWER(T.title) LIKE %:title% GROUP BY id ORDER BY available DESC", nativeQuery = true)
    List<Object[]> findAllByTitleContainingSortByAvailabilityImpl(@Param("start") Long startDate, @Param("title") String title);

    default List<Trip> findAllByTitleContainingSortByAvailability(Long startDate, String title) {

        List<Trip> trips = new ArrayList<>();
        List<Object[]> counts = findAllByTitleContainingSortByAvailabilityImpl(startDate, title.toLowerCase());

        if (counts == null)
            return null;

        counts.forEach(entry -> trips.add(findTripById(UUID.fromString((String) entry[0]))));

        return trips;
    }

}
