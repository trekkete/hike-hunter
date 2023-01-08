package it.trekkete.ui.views.esplora;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import it.trekkete.data.entity.Trip;
import it.trekkete.data.service.*;
import it.trekkete.security.AuthenticatedUser;
import it.trekkete.ui.views.MainLayout;
import it.trekkete.ui.views.parti.PartiView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@PageTitle("Tutte le escursioni")
@Route(value = "all-trips", layout = MainLayout.class)
@AnonymousAllowed
public class SearchView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final TripRepository tripRepository;
    private final TripParticipantsRepository tripParticipantsRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final TripLocationRepository tripLocationRepository;

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
        //getStyle().set("background-image", "url('images/background.png')");
        getStyle().set("background-color", "#00680082");

        VerticalLayout container = new VerticalLayout();
        container.addClassNames("esplora-view", "main-container");

        HorizontalLayout headerContainer = new HorizontalLayout();
        headerContainer.setWidthFull();
        headerContainer.setAlignItems(FlexComponent.Alignment.BASELINE);
        headerContainer.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H2 header = new H2("Cerca tra tutte le escursioni");
        header.addClassNames("mb-0", "mt-xl", "text-3xl");
        header.getStyle().set("margin-top", "0");

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Ordina per");
        sortBy.setItems("Popolarità", "Più recenti", "Meno recenti");
        sortBy.setValue("Popolarità");

        headerContainer.add(header, sortBy);

        List<Trip> trips = tripRepository.findAll();

        /*for (int i = 0; i < 10; i++) {
            trips.add(generateRandomTrip());
        }*/

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

            HorizontalLayout imageContainer = new HorizontalLayout();
            imageContainer.setWidthFull();
            imageContainer.addClassNames("gap-m", "m-0", "list-none", "p-0");
            imageContainer.getStyle().set("overflow-x", "scroll").set("padding-bottom", "0.5em").set("flex-wrap", "wrap");

            for (Trip t : trips) {
                imageContainer.add(new EsploraViewCard(t, authenticatedUser, userRepository, tripParticipantsRepository, tripLocationRepository, locationRepository));
            }

            verticalLayout.add(imageContainer);
        }

        container.add(headerContainer, verticalLayout);
        add(container);
    }
}
