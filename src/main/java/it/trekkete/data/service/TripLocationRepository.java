package it.trekkete.data.service;

import it.trekkete.data.entity.TripLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripLocationRepository extends JpaRepository<TripLocation, UUID> {

    List<TripLocation> findAllByTripOrderByIndex(UUID trip);

    List<TripLocation> findAllByLocation(UUID location);

    List<TripLocation> findByTripAndIndex(UUID trip, Integer index);
}
