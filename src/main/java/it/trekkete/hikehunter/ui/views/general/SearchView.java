package it.trekkete.hikehunter.ui.views.general;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import elemental.json.JsonValue;
import it.trekkete.hikehunter.data.entity.Location;
import it.trekkete.hikehunter.data.entity.Trip;
import it.trekkete.hikehunter.data.service.*;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.components.TripCard;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.ui.views.logged.CreateTripView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.List;

@PageTitle("Tutte le escursioni")
@Route(value = "all-trips", layout = MainLayout.class)
@AnonymousAllowed
public class SearchView extends VerticalLayout {

    private enum Sorting {
        AVAILABILITY("Posti disponibili"),
        OLDEST("Meno recenti"),
        NEWEST("Più recenti"),
        DIFFICULTY("Difficoltà");

        private String label;

        Sorting(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final AuthenticatedUser authenticatedUser;
    private final TripRepository tripRepository;
    private final TripParticipantsRepository tripParticipantsRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final TripLocationRepository tripLocationRepository;

    private List<Trip> trips;
    private VerticalLayout tripContainer;

    private boolean isLocalized;
    private Location userLocation;

    public SearchView(@Autowired AuthenticatedUser authenticatedUser,
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

        setMinHeight("100%");

        VerticalLayout container = new VerticalLayout();

        HorizontalLayout headerContainer = new HorizontalLayout();
        headerContainer.setWidthFull();
        headerContainer.setAlignItems(FlexComponent.Alignment.BASELINE);
        headerContainer.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H2 header = new H2("Cerca tra tutte le escursioni");
        header.addClassNames("mb-0", "mt-xl", "text-3xl");
        header.getStyle().set("margin-top", "0");

        TextField searchField = new TextField("Nome escursione");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);

        Select<Sorting> sortBy = new Select<>();
        sortBy.setLabel("Ordina per");
        sortBy.setItems(Sorting.values());
        sortBy.setValue(Sorting.AVAILABILITY);
        sortBy.setItemLabelGenerator(Sorting::getLabel);

        searchField.addValueChangeListener(event -> filterItems(event.getValue(), sortBy.getValue()));
        sortBy.addValueChangeListener(event -> filterItems(searchField.getValue(), event.getValue()));

        headerContainer.add(header, new HorizontalLayout(searchField, sortBy));

        trips = tripRepository.findAll();

        tripContainer = new VerticalLayout();
        tripContainer.setPadding(false);

        add(tripContainer);

        container.add(headerContainer, tripContainer);
        add(container);

        updateUI();
    }

    private void updateUI() {

        tripContainer.removeAll();

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

                        tripContainer.setAlignItems(FlexComponent.Alignment.CENTER);
                        tripContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

                        H3 empty = new H3("Non ci sono escursioni al momento :(");
                        empty.getStyle().set("color", "gray");

                        Button create = new Button("Crea un'escursione!");
                        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                        create.addClickListener(click -> UI.getCurrent().navigate(CreateTripView.class));

                        tripContainer.add(empty, create);
                    }
                    else {

                        HorizontalLayout imageContainer = new HorizontalLayout();
                        imageContainer.setWidthFull();
                        imageContainer.addClassNames("gap-m", "m-0", "list-none", "p-0");
                        imageContainer.getStyle().set("overflow-x", "scroll").set("padding", "0 0 1em 0.5em").set("flex-wrap", "wrap");

                        for (Trip t : trips) {
                            imageContainer.add(new TripCard(t, authenticatedUser,
                                    userRepository, tripParticipantsRepository,
                                    tripLocationRepository, locationRepository,
                                    isLocalized, userLocation));
                        }

                        tripContainer.add(imageContainer);
                    }
                });
    }

    private void filterItems(String search, Sorting sort) {

        if (search == null || search.isEmpty())
            search = "";

        switch (sort) {
            case AVAILABILITY -> {
                trips = tripRepository.findAllByTitleContainingSortByAvailability(search);
            }
            case NEWEST -> {
                trips = tripRepository.findAllByTitleContaining(search, Sort.by(Sort.Direction.DESC, "startDate"));
            }
            case OLDEST -> {
                trips = tripRepository.findAllByTitleContaining(search, Sort.by(Sort.Direction.ASC, "startDate"));
            }
            case DIFFICULTY -> {
                trips = tripRepository.findAllByTitleContaining(search, Sort.by(Sort.Direction.DESC, "rating"));
            }
        }

        updateUI();
    }
}
