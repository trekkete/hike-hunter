package it.trekkete.hikehunter.ui.views.logged;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.data.entity.*;
import it.trekkete.hikehunter.data.service.LocationRepository;
import it.trekkete.hikehunter.data.service.TripLocationRepository;
import it.trekkete.hikehunter.data.service.TripParticipantsRepository;
import it.trekkete.hikehunter.data.service.TripRepository;
import it.trekkete.hikehunter.map.LGeoJSONProperties;
import it.trekkete.hikehunter.map.LMap;
import it.trekkete.hikehunter.map.LOverpassLayer;
import it.trekkete.hikehunter.overpass.OverpassQueryBuilder;
import it.trekkete.hikehunter.overpass.OverpassQueryOptions;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.ui.views.general.HomeView;
import it.trekkete.hikehunter.ui.window.QueryResultWindow;
import it.trekkete.hikehunter.utils.AppEvents;
import kong.unirest.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import software.xdev.vaadin.maps.leaflet.flow.data.LTileLayer;

import javax.annotation.security.PermitAll;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@PageTitle("Parti")
@Route(value = "new-trip", layout = MainLayout.class)
@PermitAll
public class CreateTripView extends VerticalLayout {

    private final Logger log = LogManager.getLogger(CreateTripView.class);

    public enum TripMode {
        GIORNATA,
        MULTI
    }

    private final AuthenticatedUser authenticatedUser;
    private final LocationRepository locationRepository;
    private final TripRepository tripRepository;
    private final TripLocationRepository tripLocationRepository;
    private final TripParticipantsRepository tripParticipantsRepository;

    private TextField title;
    private TextArea description;
    private DatePicker startDate;
    private DatePicker endDate;
    private RadioButtonGroup<TripMode> tripMode;
    private Select<Integer> rating;
    private IntegerField maxNumber;
    private LMap map;

    private final VerticalLayout container;

    public CreateTripView(@Autowired AuthenticatedUser authenticatedUser,
                          @Autowired TripRepository tripRepository,
                          @Autowired TripParticipantsRepository tripParticipantsRepository,
                          @Autowired LocationRepository locationRepository,
                          @Autowired TripLocationRepository tripLocationRepository) {
        this.authenticatedUser = authenticatedUser;
        this.tripRepository = tripRepository;
        this.tripParticipantsRepository = tripParticipantsRepository;
        this.locationRepository = locationRepository;
        this.tripLocationRepository = tripLocationRepository;

        this.container = new VerticalLayout();
        this.container.setSizeFull();
        this.container.setSpacing(false);
        this.container.addClassNames(LumoUtility.AlignItems.CENTER);

        add(container);
    }

    private void constructUI() {

        setSpacing(false);
        setSizeFull();
        addClassNames(LumoUtility.AlignItems.CENTER);

        WrappedSession session = UI.getCurrent().getSession().getSession();
        if (session != null
                && session.getAttribute(AppEvents.CREATE_TRIP_STAGE) != null
                && session.getAttribute(AppEvents.CREATE_TRIP_OBJ) != null) {

            Trip trip = (Trip) session.getAttribute(AppEvents.CREATE_TRIP_OBJ);
            Integer stage = (Integer) session.getAttribute(AppEvents.CREATE_TRIP_STAGE);

            switch (stage) {
                case 2 -> setStageTwo(trip);
                case 3 -> setStageThree(trip);
                default -> setStageOne(trip);
            }
        }
        else {
            setStageOne(new Trip());
        }

        add(container);
    }

    private void setStageOne(Trip trip) {

        setPadding(false);

        container.removeAll();
        container.setPadding(false);
        container.addClassNames(LumoUtility.Position.RELATIVE);

        H4 stageTitle = new H4("Imposta l'itinerario");
        stageTitle.addClassNames(LumoUtility.Margin.NONE,
                LumoUtility.Padding.Horizontal.LARGE,
                LumoUtility.Padding.Vertical.XSMALL,
                LumoUtility.Position.ABSOLUTE,
                LumoUtility.BorderRadius.MEDIUM);
        stageTitle.getStyle()
                .set("border", "2px solid rgba(0,0,0,.2)")
                .set("background-clip", "padding-box")
                .set("background-color", "white")
                .set("z-index", "99")
                .set("top", "10px");

        container.add(stageTitle);

        Button open = new Button(new Icon("fas", "0"));
        open.setWidth("55px");
        open.setHeight("55px");
        open.getStyle().set("border", "2px solid rgba(0,0,0,.2)")
                .set("background-clip", "padding-box")
                .set("border-radius", "50%")
                .set("background-color", "white");;
        open.setEnabled(false);

        Button next = new Button(FontAwesome.Solid.CHECK.create());
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        next.setWidth("55px");
        next.setHeight("55px");
        next.getStyle().set("border", "2px solid rgba(0,0,0,.2)")
                .set("background-clip", "padding-box")
                .set("border-radius", "50%");
        next.addClickListener(click -> {

            UI.getCurrent().getSession().getSession().setAttribute(AppEvents.CREATE_TRIP_OBJ, trip);
            UI.getCurrent().getSession().getSession().setAttribute(AppEvents.CREATE_TRIP_STAGE, 2);

            setStageTwo(trip);
        });

        ContextMenu info = new ContextMenu(open);
        info.setOpenOnClick(true);

        map = new LMap(LMap.Locations.ROME);
        map.setTileLayer(LMap.Layers.DEFAULT_OPENSTREETMAP);
        map.getElement().executeJs("this.map.options.minZoom = 6;");
        map.setWidthFull();
        map.setMinHeight("100%");
        map.setHeight("900px");
        map.addClickListener((lat, lon) -> {

            String query = new OverpassQueryBuilder()
                    .setOutput(OverpassQueryOptions.Output.GEOM, OverpassQueryOptions.Output.QT)
                    .setLimit(5)
                    .setQuery("nwr(around:300," + lat + "," + lon + ")[~\"^name|^ref|^type\"~\".*\"][!information][!landuse][!boundary](if: count_tags() > 1);").build();

            log.trace("click query: {}", query);

            LOverpassLayer overpassLayer = map.addOverpassLayer();
            overpassLayer.query(query);

            map.addData(overpassLayer.toGeoJSON(
                    (feature) -> {
                        return new LGeoJSONProperties();
                    },
                    (feature) -> {
                        JSONObject element = (JSONObject) feature;

                        if (element.has("tags") && element.getJSONObject("tags").has("name")) {
                            return new Text(element.getJSONObject("tags").getString("name"));
                        } else if (element.has("name")) {
                            return new Text(element.getString("name"));
                        } else if (element.has("id")) {
                            return new Text(element.getString("id"));
                        }

                        return new Text("Sconosciuto");
                    }
            ));

            open.setIcon(new Icon("fas", String.valueOf(overpassLayer.getResults().size())));
            open.setEnabled(!overpassLayer.getResults().isEmpty());

            if (overpassLayer.getResults().isEmpty()) {
                return;
            }

            info.removeAll();

            QueryResultWindow qrw = new QueryResultWindow(overpassLayer.getResults());
            qrw.addCloseListener(click -> {
                info.close();
            });

            info.add(qrw);
        });

        VerticalLayout bottomButtonsContainer = new VerticalLayout(open, next);
        bottomButtonsContainer.setPadding(false);
        bottomButtonsContainer.setSpacing(false);
        bottomButtonsContainer.setSizeUndefined();
        bottomButtonsContainer.addClassNames(LumoUtility.Position.ABSOLUTE);
        bottomButtonsContainer.getStyle()
                .set("bottom", "40px")
                .set("right", "10px")
                .set("z-index", "99");

        container.add(bottomButtonsContainer);

        container.add(map);
    }

    private void setStageTwo(Trip trip) {

        setPadding(true);

        container.setPadding(true);
        container.removeAll();

        H4 stageTitle = new H4("Informazioni generali");
        stageTitle.addClassNames(LumoUtility.Margin.NONE);

        container.add(stageTitle);

        FormLayout stageTwoLayout = new FormLayout();
        stageTwoLayout.setWidthFull();
        stageTwoLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        title = new TextField("Titolo");
        title.setMaxLength(30);
        title.setRequired(true);
        title.setPlaceholder("Un titolo dell'escursione");
        if (trip.getTitle() != null) {
            title.setValue(trip.getTitle());
        }

        description = new TextArea("Descrizione");
        description.setMinHeight("200px");
        description.setMaxHeight("300px");
        description.setPlaceholder("Una descrizione sommaria del viaggio");
        if (trip.getDescription() != null) {
            description.setValue(trip.getDescription());
        }

        startDate = new DatePicker("Partenza");
        startDate.setMin(LocalDate.now());
        startDate.setWidthFull();
        startDate.setRequired(true);
        if (trip.getStartDate() != null) {
            startDate.setValue(LocalDate.ofInstant(Instant.ofEpochSecond(trip.getStartDate()), ZoneId.systemDefault()));
        }

        endDate = new DatePicker("Ritorno");
        endDate.setMin(LocalDate.now());
        endDate.setWidthFull();
        if (trip.getEndDate() != null) {
            endDate.setValue(LocalDate.ofInstant(Instant.ofEpochSecond(trip.getEndDate()), ZoneId.systemDefault()));
        }

        tripMode = new RadioButtonGroup<>("Durata");
        tripMode.setItemLabelGenerator(mode -> {
            switch (mode)  {
                case GIORNATA -> {
                    return "In giornata";
                }
                case MULTI -> {
                    return "Più giorni";
                }
            }

            return "?";
        });
        tripMode.setItems(TripMode.values());
        if (trip.getStartDate() != null && trip.getEndDate() != null) {
            if (startDate.getValue().isEqual(endDate.getValue())) {
                tripMode.setValue(TripMode.GIORNATA);
            }
            else {
                tripMode.setValue(TripMode.MULTI);
            }
        }
        else {
            tripMode.setValue(TripMode.GIORNATA);
        }

        HorizontalLayout dates = new HorizontalLayout(startDate);
        dates.getStyle().set("flex-wrap", "wrap");
        dates.setSpacing(false);

        tripMode.addValueChangeListener(event -> {

            if (event.getValue().equals(TripMode.GIORNATA))
                dates.remove(endDate);
            else
                dates.add(endDate);

        });

        stageTwoLayout.add(title, description, tripMode, dates);

        Button back = new Button("Indietro");
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.getStyle().set("margin-top", "1.5em");
        back.addClickListener(click -> setStageOne(trip));

        Button next = new Button("Avanti");
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        next.addClickListener(click -> {

            if (title.isInvalid()) {
                return;
            }

            trip.setTitle(title.getValue().trim());

            if (description.isInvalid()) {
                return;
            }

            trip.setDescription(description.getValue().trim());

            trip.setStartDate(startDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond());

            if (tripMode.getValue().equals(TripMode.GIORNATA)) {
                trip.setEndDate(startDate.getValue().atTime(23, 59).atZone(ZoneId.systemDefault()).toEpochSecond());
            }
            else {

                if (endDate.getValue().isBefore(startDate.getValue())) {
                    endDate.setInvalid(true);
                    endDate.setErrorMessage("La data di arrivo non può precedere la partenza");

                    return;
                }

                trip.setEndDate(endDate.getValue().atTime(23, 59).atZone(ZoneId.systemDefault()).toEpochSecond());
            }

            UI.getCurrent().getSession().getSession().setAttribute(AppEvents.CREATE_TRIP_OBJ, trip);
            UI.getCurrent().getSession().getSession().setAttribute(AppEvents.CREATE_TRIP_STAGE, 3);

            setStageThree(trip);
        });

        container.add(stageTwoLayout);

        container.addAndExpand(new Span());

        FormLayout stageTwoButtons = new FormLayout();
        stageTwoButtons.setWidthFull();
        stageTwoButtons.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        stageTwoButtons.add(back, next);

        container.add(stageTwoButtons);
    }

    private void setStageThree(Trip trip) {

        container.removeAll();

        H4 stageTitle = new H4("Dettagli dell'itinerario");
        stageTitle.addClassNames(LumoUtility.Margin.NONE);

        container.add(stageTitle);

        FormLayout stageThreeLayout = new FormLayout();
        stageThreeLayout.setWidthFull();
        stageThreeLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        rating = new Select<>();
        rating.setLabel("Difficoltà prevista");
        rating.setItems(1, 2, 3, 4, 5);
        rating.setValue(1);
        rating.setRequiredIndicatorVisible(true);
        rating.setItemLabelGenerator(Trip::formatRating);
        if (trip.getRating() != null) {
            rating.setValue(trip.getRating());
        }

        maxNumber = new IntegerField("Numero massimo di partecipanti");
        maxNumber.setMin(1);
        maxNumber.setStep(1);
        maxNumber.setHasControls(false);
        if (trip.getMaxParticipants() != null) {
            maxNumber.setValue(trip.getMaxParticipants());
        }

        MultiSelectListBox<Equipment> equipment = new MultiSelectListBox<>();
        equipment.setItems(Equipment.values());
        equipment.setItemLabelGenerator(equip -> equip.name().toLowerCase().replace("_", " "));
        if (trip.getEquipment() != null) {
            equipment.setItems(Arrays.stream(Equipment.values()).filter(e -> (trip.getEquipment() & e.getFlag()) == e.getFlag()).toList());
        }

        stageThreeLayout.add(rating, maxNumber, equipment);

        Button back = new Button("Indietro");
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.getStyle().set("margin-top", "1.5em");
        back.addClickListener(click -> setStageTwo(trip));

        Button next = new Button("Fine");
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        next.addClickListener(click -> {

            trip.setRating(rating.getValue());

            trip.setMaxParticipants(maxNumber.getValue());

            Optional<Integer> equip = equipment.getSelectedItems().stream().map(Equipment::getFlag).reduce(Integer::sum);

            equip.ifPresent(trip::setEquipment);

            trip.setCreationTs(ZonedDateTime.now().toEpochSecond());

            UUID creatorId = authenticatedUser.get().get().getId();

            trip.setCreator(creatorId);

            tripRepository.save(trip);

            TripParticipants tp = new TripParticipants();
            tp.setTrip(trip.getId());
            tp.setUser(creatorId);

            tripParticipantsRepository.save(tp);

            /*for (Location location : gridItems) {
                TripLocation tripLocation = new TripLocation();
                tripLocation.setTrip(trip.getId());
                tripLocation.setLocation(location.getId());
                tripLocation.setIndex(gridItems.indexOf(location));

                tripLocationRepository.save(tripLocation);
            }*/

            UI.getCurrent().navigate(HomeView.class);
        });

        container.add(stageThreeLayout);

        container.addAndExpand(new Span());

        FormLayout stageThreeButtons = new FormLayout();
        stageThreeButtons.setWidthFull();
        stageThreeButtons.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        stageThreeButtons.add(back, next);

        container.add(stageThreeButtons);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}
