package it.trekkete.ui.views.unisciti;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import it.trekkete.data.entity.*;
import it.trekkete.data.service.LocationRepository;
import it.trekkete.data.service.TripLocationRepository;
import it.trekkete.data.service.TripParticipantsRepository;
import it.trekkete.data.service.TripRepository;
import it.trekkete.security.AuthenticatedUser;
import it.trekkete.ui.views.MainLayout;
import it.trekkete.ui.views.esplora.EsploraView;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.UUID;

@PageTitle("Dettaglio escursione")
@Route(value = "trip/:tripId", layout = MainLayout.class)
@PermitAll
public class UniscitiView extends VerticalLayout implements BeforeEnterObserver {

    private Trip trip;

    private final TripRepository tripRepository;
    private final TripParticipantsRepository tripParticipantsRepository;
    private final AuthenticatedUser authenticatedUser;
    private final TripLocationRepository tripLocationRepository;
    private final LocationRepository locationRepository;

    private boolean alreadySubscribed;

    public UniscitiView(@Autowired AuthenticatedUser authenticatedUser,
                        @Autowired TripRepository tripRepository,
                        @Autowired TripParticipantsRepository tripParticipantsRepository,
                        @Autowired TripLocationRepository tripLocationRepository,
                        @Autowired LocationRepository locationRepository) {
        this.authenticatedUser = authenticatedUser;
        this.tripRepository = tripRepository;
        this.tripParticipantsRepository = tripParticipantsRepository;
        this.tripLocationRepository = tripLocationRepository;
        this.locationRepository = locationRepository;
    }

    public void constructUI() {

        User user = authenticatedUser.get().get();

        getStyle()
                .set("background-image", "url('images/background.png')");

        setHeightFull();

        VerticalLayout container = new VerticalLayout();
        container.addClassNames("esplora-view", "main-container");

        add(container);

        if (trip == null)
            return;

        H2 title = new H2(trip.getTitle());
        title.getStyle().set("margin-top", "0");

        H4 descriptionTitle = new H4("Descrizione");
        descriptionTitle.getStyle().set("color", "var(--lumo-contrast-30pct");
        Text desc = new Text(trip.getDescription());

        container.add(title, descriptionTitle, desc);

        VerticalLayout locationsContainer = new VerticalLayout();
        locationsContainer.setPadding(false);
        locationsContainer.setSpacing(false);
        List<TripLocation> tripLocations = tripLocationRepository.findAllByTripOrderByIndex(trip.getId());
        for (int i = 0; i < tripLocations.size(); i++) {

            H4 temp = new H4("Tappa #" + i + ":");
            temp.getStyle().set("margin-top", "0");

            Location location = locationRepository.findLocationById(tripLocations.get(i).getLocation());

            Span span = new Span(location.getName());

            HorizontalLayout horizontalLayout = new HorizontalLayout(temp, span);
            horizontalLayout.setAlignItems(Alignment.BASELINE);

            locationsContainer.add(horizontalLayout);
        }

        container.add(locationsContainer);

        Button join = new Button(alreadySubscribed ? "Annulla partecipazione" : "Partecipa");
        join.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        join.addClickListener(click -> {

            Dialog dialog = new Dialog();

            dialog.setHeaderTitle(alreadySubscribed ? "Vuoi annullare la tua partecipazione?" : "Confermi la tua partecipazione?");

            Button closeButton = new Button(new Icon("lumo", "cross"), (e) -> dialog.close());
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            dialog.getHeader().add(closeButton);

            Button cancelButton = new Button("No", (e) -> dialog.close());
            cancelButton.getStyle().set("margin-right", "auto");
            dialog.getFooter().add(cancelButton);

            Button confirmButton = new Button("Si", (e) -> {

                if (!alreadySubscribed) {

                    TripParticipants participants = new TripParticipants();
                    participants.setTrip(trip.getId());
                    participants.setUser(user.getId());

                    tripParticipantsRepository.save(participants);

                    alreadySubscribed = true;
                    join.setText("Annulla partecipazione");
                }
                else {

                    TripParticipants participants = tripParticipantsRepository.findByTripAndUser(trip.getId(), user.getId());
                    tripParticipantsRepository.delete(participants);

                    alreadySubscribed = false;
                    join.setText("Partecipa");
                }

                dialog.close();
            });
            confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(confirmButton);

            dialog.open();
        });

        HorizontalLayout footer = new HorizontalLayout(join);

        if (user.getId().equals(trip.getCreator())) {

            Button delete = new Button("Elimina", buttonClickEvent -> {

                Dialog dialog = new Dialog();

                dialog.setHeaderTitle("Cancella l'escursione");

                dialog.add(new Label("Vuoi davvero eliminare l'escursione programmata?"));

                Button closeButton = new Button(new Icon("lumo", "cross"), (e) -> dialog.close());
                closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                dialog.getHeader().add(closeButton);

                Button cancelButton = new Button("No, non eliminare", (e) -> dialog.close());
                cancelButton.getStyle().set("margin-right", "auto");
                dialog.getFooter().add(cancelButton);

                Button confirmButton = new Button("Si, elimina", (e) -> {

                    tripRepository.delete(trip);
                    tripLocationRepository.deleteAll(tripLocationRepository.findAllByTripOrderByIndex(trip.getId()));
                    tripParticipantsRepository.deleteAll(tripParticipantsRepository.findAllByTrip(trip.getId()));
                    dialog.close();

                    UI.getCurrent().navigate(EsploraView.class);
                });
                confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                dialog.getFooter().add(confirmButton);

                dialog.open();
            });

            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

            footer.add(delete);
        }
        footer.setJustifyContentMode(JustifyContentMode.END);
        footer.setWidthFull();

        container.add(footer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

        String id = beforeEnterEvent.getRouteParameters().get("tripId").get();

        this.trip = tripRepository.findById(UUID.fromString(id)).get();

        User user = authenticatedUser.get().get();

        List<TripParticipants> subscribed = tripParticipantsRepository.findAllByTrip(trip.getId());

        this.alreadySubscribed = false;
        for (TripParticipants participants : subscribed) {
            if (participants.getUser().equals(user.getId())) {
                this.alreadySubscribed = true;
                break;
            }
        }

        constructUI();
    }
}
