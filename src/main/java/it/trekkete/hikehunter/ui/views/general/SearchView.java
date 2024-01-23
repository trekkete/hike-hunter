package it.trekkete.hikehunter.ui.views.general;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
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
    private final TripParticipantsRepository tripParticipantsRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final TripLocationRepository tripLocationRepository;

    private final TripService tripService;

    private List<Trip> trips;
    private VerticalLayout tripContainer;

    public SearchView(@Autowired AuthenticatedUser authenticatedUser,
                       @Autowired TripRepository tripRepository,
                       @Autowired TripParticipantsRepository tripParticipantsRepository,
                       @Autowired UserRepository userRepository,
                      @Autowired LocationRepository locationRepository,
                      @Autowired TripLocationRepository tripLocationRepository) {

        this.authenticatedUser = authenticatedUser;
        this.tripParticipantsRepository = tripParticipantsRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.tripLocationRepository = tripLocationRepository;

        this.tripService = new TripService(tripRepository);

        constructUI();
    }

    private void constructUI() {

        setMinHeight("100%");

        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);

        FormLayout headerContainer = new FormLayout();
        headerContainer.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        H3 header = new H3("Cerca tra tutte le escursioni");
        header.getStyle().set("margin", "0");

        TextField searchField = new TextField("Nome escursione");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);

        Select<Sorting> sortBy = new Select<>();
        sortBy.setLabel("Ordina per");
        sortBy.setItems(Sorting.values());
        sortBy.setValue(Sorting.AVAILABILITY);
        sortBy.setItemLabelGenerator(Sorting::getLabel);

        searchField.addValueChangeListener(event -> filterItems(event.getValue(), sortBy.getValue()));
        sortBy.addValueChangeListener(event -> filterItems(searchField.getValue(), event.getValue()));

        headerContainer.add(searchField, sortBy);

        trips = tripService.findAllAvailable(150);

        tripContainer = new VerticalLayout();
        tripContainer.setPadding(false);

        container.add(header, headerContainer, tripContainer);
        add(container);

        updateUI();
    }

    private void updateUI() {

        tripContainer.removeAll();

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
            imageContainer.getStyle().set("flex-wrap", "wrap");

            for (Trip t : trips) {
                TripCard tripCard = new TripCard(t, authenticatedUser,
                        userRepository, tripParticipantsRepository,
                        tripLocationRepository, locationRepository);
                tripCard.setMaxWidth("100%");
                imageContainer.add(tripCard);
            }

            tripContainer.add(imageContainer);
        }
    }

    private void filterItems(String search, Sorting sort) {

        if (search == null || search.isEmpty())
            search = "";

        switch (sort) {
            case AVAILABILITY -> {
                trips = tripService.findAllWithAvailability(search);
            }
            case NEWEST -> {
                trips = tripService.findAllContaining(search, Sort.by(Sort.Direction.DESC, "startDate"));
            }
            case OLDEST -> {
                trips = tripService.findAllContaining(search, Sort.by(Sort.Direction.ASC, "startDate"));
            }
            case DIFFICULTY -> {
                trips = tripService.findAllContaining(search, Sort.by(Sort.Direction.DESC, "rating"));
            }
        }

        updateUI();
    }
}
