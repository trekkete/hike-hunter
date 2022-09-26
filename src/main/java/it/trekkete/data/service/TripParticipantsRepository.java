package it.trekkete.data.service;

import it.trekkete.data.entity.TripParticipants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripParticipantsRepository extends JpaRepository<TripParticipants, UUID> {

    Integer countAllByTrip(UUID trip);

    Integer countAllByUser(UUID user);

    List<TripParticipants> findAllByTrip(UUID trip);

    List<TripParticipants> findAllByUser(UUID trip);

    TripParticipants findByTripAndUser(UUID trip, UUID user);
}
