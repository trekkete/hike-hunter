package it.trekkete.hikehunter.data.service;

import it.trekkete.hikehunter.data.entity.UserRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRatingRepository extends JpaRepository<UserRating, UUID> {

    List<UserRating> findAllByFromAndTrip(UUID from, UUID trip);
    List<UserRating> findAllByAboutAndTrip(UUID to, UUID trip);

}
