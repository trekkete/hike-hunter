package it.trekkete.data.service;

import it.trekkete.data.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Location findLocationByName(String name);

    Location findLocationById(String id);
}