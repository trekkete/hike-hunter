package it.trekkete.hikehunter.ui.views.logged;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.trekkete.hikehunter.data.entity.Location;
import it.trekkete.hikehunter.data.entity.Trip;
import it.trekkete.hikehunter.data.entity.TripLocation;
import it.trekkete.hikehunter.data.entity.TripParticipants;
import it.trekkete.hikehunter.data.service.LocationRepository;
import it.trekkete.hikehunter.data.service.TripLocationRepository;
import it.trekkete.hikehunter.data.service.TripParticipantsRepository;
import it.trekkete.hikehunter.data.service.TripRepository;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.ui.views.general.HomeView;
import it.trekkete.hikehunter.utils.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import software.xdev.vaadin.maps.leaflet.flow.LMap;
import software.xdev.vaadin.maps.leaflet.flow.data.LMarker;
import software.xdev.vaadin.maps.leaflet.flow.data.LTileLayer;

import javax.annotation.security.PermitAll;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@PageTitle("Parti")
@Route(value = "new-trip", layout = MainLayout.class)
@PermitAll
public class CreateTripView extends VerticalLayout {

    public enum TripMode {
        GIORNATA,
        MULTI
    }

    private LocationRepository locationRepository;

    private TextField title;
    private TextArea description;
    private DatePicker startDate;
    private DatePicker endDate;
    private RadioButtonGroup<TripMode> tripMode;
    private Select<Integer> rating;
    private TextField maxNumber;
    private TextField searchLocation;
    private LMap map;
    private Grid<Location> locationGrid;

    private List<Location> gridItems;
    private Map<Location, LMarker> markerMap;

    private Map<String, String> equipmentMap;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    public CreateTripView(@Autowired AuthenticatedUser authenticatedUser,
                          @Autowired TripRepository tripRepository,
                          @Autowired TripParticipantsRepository tripParticipantsRepository,
                          @Autowired LocationRepository locationRepository,
                          @Autowired TripLocationRepository tripLocationRepository) {

        this.locationRepository = locationRepository;
        this.gridItems = new ArrayList<>();
        this.markerMap = new HashMap<>();
        this.equipmentMap = new HashMap<>();

        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setAlignItems(Alignment.CENTER);

        container.add(createTitle());
        container.add(createFormLayout());
        container.add(createButtonLayout());

        save.addClickListener(click -> {

            if (!validateForm())
                return;

            Trip newTrip = new Trip();
            newTrip.setTitle(title.getValue());
            newTrip.setDescription(description.getValue());

            newTrip.setCreationTs(ZonedDateTime.now().toEpochSecond());

            newTrip.setRating(rating.getValue());

            newTrip.setEquipment(new Gson().toJson(equipmentMap));

            if (maxNumber.getValue() != null && !maxNumber.isEmpty())
                newTrip.setMaxParticipants(Integer.parseInt(maxNumber.getValue()));

            UUID creatorId = authenticatedUser.get().get().getId();

            newTrip.setCreator(creatorId);

            newTrip.setStartDate(startDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond());

            if (tripMode.getValue().equals(TripMode.GIORNATA))
                newTrip.setEndDate(startDate.getValue().atTime(23, 59).atZone(ZoneId.systemDefault()).toEpochSecond());
            else
                newTrip.setEndDate(endDate.getValue().atTime(23, 59).atZone(ZoneId.systemDefault()).toEpochSecond());

            tripRepository.save(newTrip);

            TripParticipants tp = new TripParticipants();
            tp.setTrip(newTrip.getId());
            tp.setUser(creatorId);

            tripParticipantsRepository.save(tp);

            for (Location location : gridItems) {
                TripLocation tripLocation = new TripLocation();
                tripLocation.setTrip(newTrip.getId());
                tripLocation.setLocation(location.getId());
                tripLocation.setIndex(gridItems.indexOf(location));

                tripLocationRepository.save(tripLocation);
            }

            clearForm();

            UI.getCurrent().navigate(HomeView.class);
        });

        cancel.addClickListener(click -> UI.getCurrent().navigate(HomeView.class));

        add(container);
    }

    private Component createTitle() {
        H2 title = new H2("Crea un'escursione");
        title.getStyle().set("margin", "0");

        return title;
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();

        title = new TextField("Titolo");
        title.setMaxLength(30);
        title.setRequired(true);
        title.setPlaceholder("Un titolo dell'escursione");

        description = new TextArea("Descrizione");
        description.setMinHeight("200px");
        description.setMaxHeight("300px");
        description.setPlaceholder("Una descrizione sommaria del viaggio");

        startDate = new DatePicker("Partenza");
        startDate.setMin(LocalDate.now());
        startDate.setWidthFull();
        startDate.setRequired(true);

        endDate = new DatePicker("Ritorno");
        endDate.setMin(LocalDate.now());
        endDate.setWidthFull();

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
        tripMode.setValue(TripMode.GIORNATA);

        HorizontalLayout dates = new HorizontalLayout(startDate);
        dates.getStyle().set("flex-wrap", "wrap");
        dates.setSpacing(false);

        formLayout.add(title, description, tripMode, dates);
        formLayout.setColspan(title, 2);
        formLayout.setColspan(description, 2);

        tripMode.addValueChangeListener(event -> {

            if (event.getValue().equals(TripMode.GIORNATA))
                dates.remove(endDate);
            else
                dates.add(endDate);

        });

        rating = new Select<>();
        rating.setLabel("Difficoltà prevista");
        rating.setItems(1, 2, 3, 4, 5);
        rating.setValue(1);
        rating.setRequiredIndicatorVisible(true);
        rating.setItemLabelGenerator(Trip::formatRating);

        maxNumber = new TextField("Numero massimo di partecipanti");

        formLayout.add(rating, maxNumber);

        H4 optionsTitle = new H4("Materiale richiesto");

        HorizontalLayout optionLayout = new HorizontalLayout();
        optionLayout.setWidthFull();
        optionLayout.setAlignItems(Alignment.CENTER);
        optionLayout.getStyle().set("overflow", "scroll");

        optionLayout.add(createOptionLayout("shoes"));
        optionLayout.add(createOptionLayout("harness"));
        optionLayout.add(createOptionLayout("helmet"));
        optionLayout.add(createOptionLayout("kit"));
        optionLayout.add(createOptionLayout("picozza"));
        optionLayout.add(createOptionLayout("rope"));
        optionLayout.add(createOptionLayout("crampons"));
        optionLayout.add(createOptionLayout("snowshoes"));

        formLayout.add(optionsTitle, 2);
        formLayout.add(optionLayout, 2);

        H4 itinerari = new H4("Aggiungi itinerari");

        searchLocation = new TextField();
        searchLocation.setValueChangeMode(ValueChangeMode.EAGER);
        searchLocation.setPlaceholder("Cerca un itinerario");
        searchLocation.getStyle().set("padding", "0");
        searchLocation.getStyle().set("margin-bottom", "1em");

        Grid<Location> searchResultsGrid = new Grid<>();
        searchResultsGrid.setWidthFull();
        searchResultsGrid.setAllRowsVisible(true);
        searchResultsGrid.setVisible(false);
        searchResultsGrid.addColumn(Location::getName).setHeader("Fai doppio-click sull'itinerario da aggiungere");
        searchResultsGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        searchResultsGrid.addItemDoubleClickListener(click -> {
            Location clickItem = click.getItem();

            locationRepository.save(clickItem);

            gridItems.add(clickItem);
            locationGrid.setItems(gridItems);

            MapUtils.fitBounds(map, gridItems.toArray(new Location[0]));

            LMarker marker = new LMarker(clickItem.getLatitude(), clickItem.getLongitude());

            map.addLComponents(marker);
            markerMap.put(clickItem, marker);

            searchLocation.clear();
        });

        searchResultsGrid.getStyle().set("margin-bottom", "1em");

        searchLocation.addValueChangeListener(event -> {

            List<Location> locations = MapUtils.getBestMatch(event.getValue());

            searchResultsGrid.setItems(locations);

            if (locations.size() > 0) {
                searchResultsGrid.setVisible(true);
                searchLocation.getStyle().set("margin-bottom", "0");
            }
            else {
                searchResultsGrid.setVisible(false);
                searchLocation.getStyle().set("margin-bottom", "1em");
            }
        });

        map = new LMap(45, 10, 7);
        map.setTileLayer(LTileLayer.DEFAULT_OPENSTREETMAP_TILE);
        map.getElement().executeJs("this.map.options.minZoom = 6;");
        map.setSizeFull();

        VerticalLayout mapContainer = new VerticalLayout(map);
        mapContainer.setSizeFull();
        mapContainer.setPadding(false);
        mapContainer.setMinHeight("400px");
        mapContainer.setHeight("0px");

        locationGrid = new Grid<>();
        locationGrid.setSizeFull();
        locationGrid.addColumn(Location::getName).setHeader("Itinerari");
        locationGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        locationGrid.addComponentColumn(location -> {

            Button delete = new Button(new Icon(VaadinIcon.TRASH));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            delete.addClickListener(click -> {

                gridItems.remove(location);
                locationGrid.setItems(gridItems);

                MapUtils.fitBounds(map, gridItems.toArray(new Location[0]));
                map.removeLComponents(markerMap.get(location));

            });

            return delete;
        }).setFlexGrow(0);
        locationGrid.setMinHeight("200px");

        formLayout.add(itinerari, 2);
        formLayout.add(searchLocation, 2);
        formLayout.add(searchResultsGrid, 2);
        formLayout.add(mapContainer, 1);
        formLayout.add(locationGrid, 1);

        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);

        return buttonLayout;
    }

    private Component createOptionLayout(String option) {

        VerticalLayout optionLayout = new VerticalLayout();
        optionLayout.addClassNames("option-layout", "deselected");

        Image deselected = new Image("images/" + option + "-deselected.png", option + "-deselected");
        Image selected = new Image("images/" + option + "-selected.png", option + "-selected");

        optionLayout.add(deselected);

        optionLayout.addClickListener(click -> {

            optionLayout.removeAll();
            if (optionLayout.hasClassName("deselected")) {
                optionLayout.removeClassName("deselected");
                optionLayout.addClassName("selected");

                equipmentMap.put(option, "true");

                optionLayout.add(selected);
            }
            else if (optionLayout.hasClassName("selected")) {
                optionLayout.removeClassName("selected");
                optionLayout.addClassName("deselected");

                equipmentMap.put(option, "false");

                optionLayout.add(deselected);
            }
        });

        optionLayout.getElement().setAttribute("title", option);

        return optionLayout;
    }

    private boolean validateForm() {

        boolean valid = true;

        if (title.getValue() == null || title.isEmpty()) {
            title.setInvalid(true);
            title.setErrorMessage("Inserisci un titolo");

            valid = false;
        }

        if (startDate.getValue() == null || startDate.isEmpty()) {
            startDate.setInvalid(true);
            startDate.setErrorMessage("Inseriesci una data di partenza");

            valid = false;
        }


        if (rating.getValue() == null || rating.isEmpty()) {
            rating.setInvalid(true);
            rating.setErrorMessage("Inserisci una difficoltà stimata");

            valid = false;
        }

        if (gridItems.isEmpty()) {
            searchLocation.setInvalid(true);
            searchLocation.setErrorMessage("Inserisci almeno un itinerario");

            valid = false;
        }


        return valid;
    }

    private void clearForm() {
       title.clear();
       description.clear();
       startDate.clear();
       endDate.clear();
       tripMode.setValue(TripMode.GIORNATA);
       rating.clear();
       maxNumber.clear();

       gridItems.clear();
       locationGrid.setItems(gridItems);

    }

}
