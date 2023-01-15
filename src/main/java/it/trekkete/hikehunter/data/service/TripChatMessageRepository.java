package it.trekkete.hikehunter.data.service;

import it.trekkete.hikehunter.data.entity.TripChatMessage;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripChatMessageRepository extends JpaRepository<TripChatMessage, UUID> {

    List<TripChatMessage> findByTrip(UUID trip, Sort sort);

    List<TripChatMessage> findByTripAndUser(UUID trip, UUID user);
}
