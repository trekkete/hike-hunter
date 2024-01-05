package it.trekkete.hikehunter.ui.views.general;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import elemental.json.JsonValue;
import it.trekkete.hikehunter.data.entity.Location;
import it.trekkete.hikehunter.data.entity.Trip;
import it.trekkete.hikehunter.data.entity.User;
import it.trekkete.hikehunter.data.service.*;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.components.TripCard;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.ui.views.logged.CreateTripView;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@PageTitle("Esplora")
@Route(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final TripRepository tripRepository;
    private final TripParticipantsRepository tripParticipantsRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final TripLocationRepository tripLocationRepository;

    private boolean isLocalized;
    private Location userLocation;

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

        constructUI();
    }

    private void constructUI() {

        setPadding(false);

        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);

        H3 header = new H3("Scopri la montagna e parti all'avventura");
        header.getStyle().set("margin", "0").set("padding", "0.3em");

        List<Trip> trips = tripRepository.findAll();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setPadding(false);

        add(verticalLayout);

        getElement().executeJs("return window.trekkete.coords;")
                .then(JsonValue.class, result -> {

                    if (result != null) {

                        JsonObject object = new Gson().fromJson(result.toJson(), JsonObject.class);

                        isLocalized = true;

                        userLocation = new Location();
                        userLocation.setLatitude(object.get("lat").getAsDouble());
                        userLocation.setLongitude(object.get("lon").getAsDouble());
                    }
                    else {
                        isLocalized = false;
                    }

                    if (trips.isEmpty()) {

                        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

                        H3 empty = new H3("Non ci sono escursioni al momento :(");
                        empty.getStyle().set("color", "gray");

                        Button create = new Button("Crea un'escursione!");
                        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                        create.addClickListener(click -> UI.getCurrent().navigate(CreateTripView.class));

                        verticalLayout.add(empty, create);
                    }
                    else {

                        createPlaylist(verticalLayout, "Le new entry", tripRepository.findAllByOrderByCreationTsDesc());
                        createPlaylist(verticalLayout, "Per iniziare", tripRepository.findAllByRatingLessThanEqual(2));

                        Optional<User> maybeUser = authenticatedUser.get();
                        if (maybeUser.isPresent()) {
                            createPlaylist(verticalLayout, "Mettiti alla prova", trips);
                        }
                    }
                });

        container.add(header, verticalLayout);
        add(container);
    }

    public void createPlaylist(FlexComponent parent, String title, List<Trip> items) {

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
                                tripLocationRepository, locationRepository,
                                isLocalized, userLocation));
            }
        }

        container.add(imageContainer);

        parent.add(container);
    }
}