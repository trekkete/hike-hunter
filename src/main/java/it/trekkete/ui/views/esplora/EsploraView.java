package it.trekkete.ui.views.esplora;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import it.trekkete.data.entity.Trip;
import it.trekkete.data.entity.User;
import it.trekkete.data.service.*;
import it.trekkete.security.AuthenticatedUser;
import it.trekkete.ui.views.MainLayout;
import it.trekkete.ui.views.parti.PartiView;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@PageTitle("Esplora")
@Route(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class EsploraView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final TripRepository tripRepository;
    private final TripParticipantsRepository tripParticipantsRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final TripLocationRepository tripLocationRepository;

    public EsploraView(@Autowired AuthenticatedUser authenticatedUser,
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
        getStyle()
                .set("background-image", "url('https://images.unsplash.com/photo-1502751106709-b3812c57da19?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1332&q=80')")
                .set("background-repeat", "no-repeat")
                .set("background-size", "cover");

        setHeightFull();

        VerticalLayout container = new VerticalLayout();
        container.addClassNames("esplora-view", "main-container");

        HorizontalLayout headerContainer = new HorizontalLayout();
        headerContainer.setWidthFull();
        headerContainer.setAlignItems(FlexComponent.Alignment.BASELINE);
        headerContainer.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H2 header = new H2("Scopri la montagna e parti all'avventura");
        header.addClassNames("mb-0", "mt-xl", "text-3xl");
        header.getStyle().set("margin-top", "0");

        Button search = new Button("Vedi tutte");
        search.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        search.addClickListener(click -> {
            UI.getCurrent().navigate(SearchView.class);
        });

        headerContainer.add(header, search);

        List<Trip> trips = tripRepository.findAll();

        for (int i = 0; i < 3; i++) {
            trips.add(generateRandomTrip());
        }

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setPadding(false);

        add(verticalLayout);
        if (trips.isEmpty()) {

            verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

            H3 empty = new H3("Non ci sono escursioni al momento :(");
            empty.getStyle().set("color", "gray");

            Button create = new Button("Crea un'escursione!");
            create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            create.addClickListener(click -> UI.getCurrent().navigate(PartiView.class));

            verticalLayout.add(empty, create);
        }
        else {

            verticalLayout.add(createPlaylist("Le new entry", tripRepository.findAllByOrderByCreationTsDesc()));
            verticalLayout.add(createPlaylist("Per iniziare", tripRepository.findAllByRatingLessThanEqual(2)));

            Optional<User> maybeUser = authenticatedUser.get();
            if (maybeUser.isPresent()) {
                verticalLayout.add(createPlaylist("Mettiti alla prova", trips));
            }
        }

        container.add(headerContainer, verticalLayout);
        add(container);
    }

    public VerticalLayout createPlaylist(String title, List<Trip> items) {

        VerticalLayout container = new VerticalLayout();
        H3 playlistTitle = new H3(title);
        playlistTitle.getStyle().set("margin-top", "0.5");

        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(false);
        container.add(playlistTitle);

        HorizontalLayout imageContainer = new HorizontalLayout();
        imageContainer.setWidthFull();
        imageContainer.addClassNames("gap-m", "m-0", "list-none", "p-0");
        imageContainer.getStyle().set("overflow-x", "scroll").set("padding-bottom", "0.5em");

        if (items == null || items.isEmpty()) {
            H4 empty = new H4("Escursioni finite, torna più tardi");
            empty.getStyle().set("color", "gray");

            imageContainer.add(empty);
        }
        else {
            for (Trip t : items) {
                imageContainer.add(new EsploraViewCard(t, authenticatedUser, userRepository, tripParticipantsRepository, tripLocationRepository, locationRepository));
            }
        }

        container.add(imageContainer);

        return container;
    }

    private Trip generateRandomTrip() {

        Trip trip = new Trip();
        trip.setTitle("Ferrata delle bocchette del brenta");
        trip.setDescription("Si sale dal percorso 105 e poi si procede costeggiando il fianco dcedlla montagna per un po' fino all'imbocco della ferrata");
        trip.setId(UUID.randomUUID());
        trip.setCreator(UUID.fromString("baa08b06-bd6b-4754-bf39-d50b7a6e5750"));
        trip.setRating(new Random().nextInt(5) + 1);
        trip.setStartDate(ZonedDateTime.now().minusDays(new Random().nextInt(0, 2)).toEpochSecond());
        trip.setEndDate(ZonedDateTime.now().toEpochSecond());
        trip.setCreationTs(ZonedDateTime.now().toEpochSecond());
        trip.setMaxParticipants(new Random().nextInt(4, 7));

        return trip;
    }
}