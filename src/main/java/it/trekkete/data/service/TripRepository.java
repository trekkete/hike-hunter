package it.trekkete.data.service;

import it.trekkete.data.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    Trip findTripById(UUID id);

    List<Trip> findAllByOrderByCreationTsDesc();

    List<Trip> findAllByRating(Integer rating);

    List<Trip> findAllByRatingLessThanEqual(Integer rating);
}
