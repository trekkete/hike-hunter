package it.trekkete.hikehunter.ui.views.logged;

import com.google.gson.Gson;
import com.googlecode.gentyref.TypeToken;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import it.trekkete.hikehunter.data.entity.Location;
import it.trekkete.hikehunter.data.entity.*;
import it.trekkete.hikehunter.data.service.*;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.components.CustomMarker;
import it.trekkete.hikehunter.ui.components.Separator;
import it.trekkete.hikehunter.ui.components.ShowMore;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.ui.views.general.HomeView;
import it.trekkete.hikehunter.map.LMap;
import it.trekkete.hikehunter.map.LOverpassLayer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@PageTitle("Dettaglio escursione")
@Route(value = "trip/:tripId", layout = MainLayout.class)
@PermitAll
public class JoinView extends VerticalLayout implements BeforeEnterObserver {

    private Trip trip;

    private final TripRepository tripRepository;
    private final TripParticipantsRepository tripParticipantsRepository;
    private final AuthenticatedUser authenticatedUser;
    private final TripLocationRepository tripLocationRepository;
    private final LocationRepository locationRepository;

    private boolean alreadySubscribed;

    private Button showChat;

    public JoinView(@Autowired AuthenticatedUser authenticatedUser,
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

        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setAlignItems(Alignment.CENTER);

        add(container);

        if (trip == null)
            return;

        H2 title = new H2(trip.getTitle());
        title.getStyle().set("margin", "0");

        ShowMore desc = new ShowMore(trip.getDescription());
        desc.setBaseHeight("205px");

        Component dateInfo = createDateInfo();

        container.add(title, dateInfo, desc);

        Separator separator = new Separator(Separator.Orientation.HORIZONTAL);

        H4 tripLocationsTitle = new H4("Itinerario");
        tripLocationsTitle.getStyle().set("margin", "0");

        container.add(separator, tripLocationsTitle);

        VerticalLayout locationsMapContainer = new VerticalLayout();
        locationsMapContainer.setPadding(false);
        locationsMapContainer.setSpacing(false);
        locationsMapContainer.setWidthFull();

        VerticalLayout locationsContainer = new VerticalLayout();
        locationsContainer.setPadding(false);
        locationsContainer.setSpacing(false);
        locationsContainer.setAlignItems(Alignment.CENTER);
        List<TripLocation> tripLocations = tripLocationRepository.findAllByTripOrderByIndex(trip.getId());
        List<Location> locations = tripLocations.stream().map(tripLocation -> locationRepository.findLocationById(tripLocation.getLocation())).toList();

        //OverpassLayer opLayer = new OverpassLayer(false, "https://overpass-api.de/api/", "area(id:3600046663)->.searchArea;nwr[\"tourism\"=\"museum\"](area.searchArea);out geom;", 8);

        LMap map = new LMap();
        map.setTileLayer(LOverpassLayer.DEFAULT_OVERPASS_TILE);
        map.getElement().executeJs("this.map.options.minZoom = 6;");
        map.setWidthFull();
        map.setMinHeight(locationsContainer.getMinHeight());
        map.setHeight("300px");
        //map.setOverpassLayer(opLayer);

        Map<Location, CustomMarker> locationMap = new HashMap<>();

        for (int i = 0; i < locations.size(); i++) {

            CustomMarker locationMarker = new CustomMarker(locations.get(i).getLatitude(), locations.get(i).getLongitude(), "%2300AEEF");
            map.addLComponents(locationMarker);

            locationMap.put(locations.get(i), locationMarker);

            String complete = locations.get(i).getName();
            String shortened = complete.substring(0, complete.indexOf(","));

            if (shortened.length() < 3) {
                shortened = complete.substring(0, complete.indexOf(",", complete.indexOf(",") + 1));
            }

            Span span = new Span(shortened);
            span.setClassName("trip-location-span");
            span.setTitle(complete);

            span.addClickListener(click -> {

                locations.forEach(other -> {
                    map.removeLComponents(locationMap.get(other));
                    locationMap.get(other).setColor("%2300AEEF");
                    map.addLComponents(locationMap.get(other));
                });

                map.removeLComponents(locationMarker);
                locationMarker.setColor("%23FFAE00");
                map.addLComponents(locationMarker);
            });

            HorizontalLayout horizontalLayout = new HorizontalLayout(span);
            horizontalLayout.addClassName("trip-location-container");

            locationsContainer.add(horizontalLayout);

            if (i < locations.size() - 1)
                locationsContainer.add(new Span(new Icon(VaadinIcon.ANGLE_DOWN)));
        }

        map.fitBounds(locations.toArray(new Location[0]));

        locationsMapContainer.add(locationsContainer, map);
        container.add(locationsMapContainer, new Separator(Separator.Orientation.HORIZONTAL));

        H4 equipmentTitle = new H4("Materiale richiesto");
        equipmentTitle.getStyle().set("margin", "0");

        container.add(equipmentTitle);

        HorizontalLayout equipmentLayout = new HorizontalLayout();
        equipmentLayout.addClassName("equipment-layout");
        equipmentLayout.getStyle().set("overflow-x", "scroll");
        equipmentLayout.setAlignItems(Alignment.CENTER);

        Map<String, String> equipmentMap;
        if (trip.getEquipment() != null)
            equipmentMap = new Gson().fromJson(trip.getEquipment(), new TypeToken< Map<String, String>>(){}.getType());
        else
            equipmentMap = new HashMap<>();

        equipmentMap.forEach((equipment, value) -> {

            if (value.equals("true")) {

                Image equip = new Image("images/" + equipment + "-selected.png", equipment);
                equip.setMaxWidth("100px");
                equip.setMaxHeight("100px");
                equip.setTitle(equipment);

                equipmentLayout.add(equip);
            }
        });

        if (equipmentLayout.getComponentCount() == 0) {
            Span noMaterial = new Span("Nessun materiale particolare richiesto");
            noMaterial.setWidthFull();
            noMaterial.getStyle().set("text-align", "center");

            equipmentLayout.add(noMaterial);
        }

        container.add(equipmentLayout);

        container.add(new Separator(Separator.Orientation.HORIZONTAL));

        showChat = new Button(VaadinIcon.CHAT.create());
        showChat.addClickListener(click -> {
            UI.getCurrent().navigate(ChatView.class, new RouteParameters("tripId", String.valueOf(trip.getId())));
        });
        showChat.addThemeVariants(ButtonVariant.LUMO_LARGE);

        showChat.setVisible(alreadySubscribed);

        Button join = new Button(alreadySubscribed ? VaadinIcon.CLOSE.create() : VaadinIcon.USER_CHECK.create());
        join.addThemeVariants(ButtonVariant.LUMO_LARGE, alreadySubscribed ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_PRIMARY);
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
                    join.setIcon(VaadinIcon.CLOSE.create());
                    join.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    join.addThemeVariants(ButtonVariant.LUMO_ERROR);
                }
                else {

                    TripParticipants participants = tripParticipantsRepository.findByTripAndUser(trip.getId(), user.getId());
                    tripParticipantsRepository.delete(participants);

                    alreadySubscribed = false;
                    join.setIcon(VaadinIcon.USER_CHECK.create());
                    join.removeThemeVariants(ButtonVariant.LUMO_ERROR);
                    join.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                }

                showChat.setVisible(alreadySubscribed);

                dialog.close();
            });
            confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(confirmButton);

            dialog.open();
        });

        HorizontalLayout footer = new HorizontalLayout(showChat, join);

        if (user.getId().equals(trip.getCreator())) {

            Button delete = new Button(VaadinIcon.TRASH.create(), buttonClickEvent -> {

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

                    UI.getCurrent().navigate(HomeView.class);
                });
                confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                dialog.getFooter().add(confirmButton);

                dialog.open();
            });

            delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_LARGE);

            footer.add(delete);
        }
        footer.setJustifyContentMode(JustifyContentMode.CENTER);
        footer.setWidthFull();

        container.add(footer);
    }

    private Component createDateInfo() {

        HorizontalLayout container = new HorizontalLayout();
        container.setPadding(false);
        container.setWidthFull();
        container.addClassName("date-info");
        container.getStyle()
                .set("box-shadow", "0 0 10px 0.005em #eee")
                .set("padding", "0.5em");

        ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(trip.getStartDate()), ZoneId.systemDefault());
        ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(trip.getEndDate()), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLL yyyy");

        String duration;
        if (ChronoUnit.DAYS.between(startTime, endTime) == 0) {
            duration = "in giornata";
        }
        else {
            duration = (ChronoUnit.DAYS.between(startTime, endTime) + 1) + " giorni";
        }

        H5 pTitle = new H5("Partenza");
        Span pSpan = new Span(formatter.format(startTime));

        H5 rTitle = new H5("Ritorno");
        Span rSpan = new Span(formatter.format(endTime));

        H5 dTitle = new H5("Durata");
        Span dSpan = new Span(duration);

        container.add(titleAndSpan(pTitle, pSpan), titleAndSpan(rTitle, rSpan), titleAndSpan(dTitle, dSpan));

        return container;
    }

    private VerticalLayout titleAndSpan(H5 title, Span span) {

        title.getStyle().set("margin", "0").set("font-size", "0.9em");

        span.getStyle().set("text-align", "center");

        VerticalLayout verticalLayout = new VerticalLayout(title, span);
        verticalLayout.setPadding(false);
        verticalLayout.setSpacing(false);
        verticalLayout.setAlignItems(Alignment.CENTER);

        return verticalLayout;
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
