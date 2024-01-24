package it.trekkete.hikehunter.ui.views.general;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import it.trekkete.hikehunter.data.entity.Location;
import it.trekkete.hikehunter.data.entity.Trip;
import it.trekkete.hikehunter.data.entity.TripLocation;
import it.trekkete.hikehunter.data.entity.User;
import it.trekkete.hikehunter.data.service.*;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.components.TripCard;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.ui.views.logged.CreateTripView;
import it.trekkete.hikehunter.utils.AppEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

@PageTitle("Esplora")
@Route(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class HomeView extends VerticalLayout implements PropertyChangeListener {

    private final Logger log = LogManager.getLogger(HomeView.class);

    private final AuthenticatedUser authenticatedUser;
    private final TripRepository tripRepository;
    private final TripParticipantsRepository tripParticipantsRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final TripLocationRepository tripLocationRepository;

    private final TripService tripService;

    private final VerticalLayout playlistContainer;
    private final Map<String, VerticalLayout> playlistMap;

    public HomeView(@Autowired AuthenticatedUser authenticatedUser,
                    @Autowired TripRepository tripRepository,
                    @Autowired TripParticipantsRepository tripParticipantsRepository,
                    @Autowired UserRepository userRepository,
                    @Autowired LocationRepository locationRepository,
                    @Autowired TripLocationRepository tripLocationRepository) {

        this.authenticatedUser = authenticatedUser;
        this.tripRepository = tripRepository;
        this.tripParticipantsRepository = tripParticipantsRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.tripLocationRepository = tripLocationRepository;

        this.tripService = new TripService(tripRepository);
        this.playlistContainer = new VerticalLayout();
        this.playlistMap = new HashMap<>();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        MainLayout.getCurrentLayout().ifPresent(mainLayout -> {
            mainLayout.addChangeListener(this);
            log.trace("Registered {} to change listener", this.getClass().getSimpleName());
        });

        constructUI();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        MainLayout.getCurrentLayout().ifPresent(mainLayout -> {
            mainLayout.removeChangeListener(this);
            log.trace("Unregistered {} to change listener", this.getClass().getSimpleName());
        });

    }

    private void constructUI() {

        setPadding(false);

        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);

        H3 header = new H3("Scopri la montagna e parti all'avventura");
        header.getStyle().set("margin", "0").set("padding", "0.3em");

        List<Trip> trips = tripService.findAllAvailable(100);
        List<Trip> challenge = new ArrayList<>();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {

            List<Trip> allByUser = tripParticipantsRepository.findAllByUser(maybeUser.get().getId()).stream().map(tripParticipants -> tripRepository.findTripById(tripParticipants.getTrip())).toList();

            int count = allByUser.size();

            int avg = 0;
            for (Trip trip : allByUser) {
                avg += trip.getRating();
            }
            avg /= count;

            int userScore = (((100 * count) / (1 + count)) + (avg / 5 * 100)) / 2;

            for (Trip trip : trips) {

                List<TripLocation> locations = tripLocationRepository.findAllByTripOrderByIndex(trip.getId());

                int locationsCount = locations.size();

                int tripScore = (((100 * locationsCount) / (1 + locationsCount)) + (trip.getRating() / 5 * 100)) / 2;

                if (tripScore > userScore && tripScore < userScore + 30) {
                    challenge.add(trip);
                }
            }

        }

        playlistContainer.setPadding(false);

        if (trips.isEmpty()) {

            playlistContainer.setAlignItems(FlexComponent.Alignment.CENTER);
            playlistContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

            H3 empty = new H3("Non ci sono escursioni al momento :(");
            empty.getStyle().set("color", "gray");

            Button create = new Button("Crea un'escursione!");
            create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            create.addClickListener(click -> UI.getCurrent().navigate(CreateTripView.class));

            playlistContainer.add(empty, create);
        }
        else {

            createPlaylist(playlistContainer, "Le new entry", tripService.findNewTrips());
            createPlaylist(playlistContainer, "Per iniziare", tripService.findEasyTrips());

            if (maybeUser.isPresent()) {
                createPlaylist(playlistContainer, "Mettiti alla prova", challenge);
            }
        }

        container.add(header, playlistContainer);
        add(container);

        MainLayout.triggerUserLocation().ifPresent(this::update);
    }

    public void createPlaylist(FlexComponent parent, String title, List<Trip> items) {

        if (playlistMap.containsKey(title)) {
            parent.remove(playlistMap.get(title));
        }

        VerticalLayout container = new VerticalLayout();
        H5 playlistTitle = new H5(title);
        playlistTitle.getStyle().set("margin", "0 0.3em");

        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(false);
        container.add(playlistTitle);

        HorizontalLayout imageContainer = new HorizontalLayout();
        imageContainer.setWidthFull();
        imageContainer.addClassNames("gap-m", "m-0", "list-none", "p-0");
        imageContainer.getStyle().set("overflow-x", "scroll").set("padding", "0.3em");

        if (items == null || items.isEmpty()) {
            return;
        }
        else {
            for (Trip t : items) {
                imageContainer.add(
                        new TripCard(t, authenticatedUser,
                                userRepository, tripParticipantsRepository,
                                tripLocationRepository, locationRepository));
            }
        }

        container.add(imageContainer);

        parent.add(container);

        playlistMap.put(title, container);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getPropertyName().equals(AppEvents.LOCATION_UPDATE)) {

            Location userLocation = (Location) propertyChangeEvent.getNewValue();

            System.out.println("Received location update event in home view for location: " + userLocation);

            update(userLocation);
        }
    }

    private void update(Location userLocation) {

        if (userLocation == null)
            return;

        createPlaylist(playlistContainer, "Vicino a te", tripService.findNearestTrips(userLocation, locationRepository, tripLocationRepository));
    }
}