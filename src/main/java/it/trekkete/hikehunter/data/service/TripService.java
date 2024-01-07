package it.trekkete.hikehunter.data.service;

import it.trekkete.hikehunter.data.entity.Location;
import it.trekkete.hikehunter.data.entity.Trip;
import it.trekkete.hikehunter.data.entity.TripLocation;
import org.apache.lucene.util.SloppyMath;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public List<Trip> findNewTrips() {
        return tripRepository.findAllByStartDateGreaterThanEqualOrderByCreationTs(System.currentTimeMillis() / 1000, Pageable.ofSize(20)).toList();
    }

    public List<Trip> findEasyTrips() {
        return tripRepository.findAllByStartDateGreaterThanEqualAndRatingLessThanEqual(System.currentTimeMillis() / 1000, 2);
    }

    public List<Trip> findAllAvailable() {
        return tripRepository.findAllByStartDateGreaterThanEqualOrderByStartDateAsc(System.currentTimeMillis() / 1000, Pageable.unpaged()).toList();
    }

    public List<Trip> findAllAvailable(int limit) {
        return tripRepository.findAllByStartDateGreaterThanEqualOrderByStartDateAsc(System.currentTimeMillis() / 1000, Pageable.ofSize(limit)).toList();
    }

    public List<Trip> findAllContaining(String title, Sort sort) {
        return tripRepository.findAllByStartDateGreaterThanEqualAndTitleContainingIgnoreCase(System.currentTimeMillis() / 1000, title, sort);
    }

    public List<Trip> findAllWithAvailability(String title) {
        return tripRepository.findAllByTitleContainingSortByAvailability(System.currentTimeMillis() / 1000, title);
    }

    public List<Trip> findNearestTrips(Location userLocation, LocationRepository locationRepository, TripLocationRepository tripLocationRepository) {
        List<Trip> all = new ArrayList<>(findAllAvailable(30));

        all.sort( (a, b) -> {

            List<TripLocation> locations = tripLocationRepository.findAllByTripOrderByIndex(a.getId());
            Location first = locationRepository.findLocationById(locations.get(0).getLocation());

            Double distanceFromA = SloppyMath.haversinMeters(userLocation.getLatitude(), userLocation.getLongitude(), first.getLatitude(), first.getLongitude());

            locations = tripLocationRepository.findAllByTripOrderByIndex(b.getId());
            first = locationRepository.findLocationById(locations.get(0).getLocation());

            Double distanceFromB = SloppyMath.haversinMeters(userLocation.getLatitude(), userLocation.getLongitude(), first.getLatitude(), first.getLongitude());

            return distanceFromA.compareTo(distanceFromB);
        });

        return all;
    }
}
