package it.trekkete.hikehunter.data.service;

import it.trekkete.hikehunter.data.entity.TripParticipants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripParticipantsRepository extends JpaRepository<TripParticipants, UUID> {

    Integer countAllByTrip(UUID trip);

    Integer countAllByUser(UUID user);

    Integer countAllByUserAndStatus(UUID user, TripParticipants.Status status);

    List<TripParticipants> findAllByTrip(UUID trip);

    List<TripParticipants> findAllByUser(UUID trip);

    List<TripParticipants> findAllByUserAndStatus(UUID user, TripParticipants.Status status);

    TripParticipants findByTripAndUser(UUID trip, UUID user);
}
