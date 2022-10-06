package it.trekkete.data.service;

import it.trekkete.data.entity.UserRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRatingRepository extends JpaRepository<UserRating, UUID> {

    List<UserRating> findAllByFromAndTrip(UUID from, UUID trip);


}
