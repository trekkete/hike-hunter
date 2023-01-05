package it.trekkete.ui.views.parti;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
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
import it.trekkete.data.entity.Location;
import it.trekkete.data.entity.Trip;
import it.trekkete.data.entity.TripLocation;
import it.trekkete.data.entity.TripParticipants;
import it.trekkete.data.service.LocationRepository;
import it.trekkete.data.service.TripLocationRepository;
import it.trekkete.data.service.TripParticipantsRepository;
import it.trekkete.data.service.TripRepository;
import it.trekkete.security.AuthenticatedUser;
import it.trekkete.ui.views.MainLayout;
import it.trekkete.ui.views.esplora.EsploraView;
import it.trekkete.utils.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import software.xdev.vaadin.maps.leaflet.flow.LMap;
import software.xdev.vaadin.maps.leaflet.flow.data.LCenter;
import software.xdev.vaadin.maps.leaflet.flow.data.LTileLayer;

import javax.annotation.security.PermitAll;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@PageTitle("Parti")
@Route(value = "new-trip", layout = MainLayout.class)
@PermitAll
public class PartiView extends VerticalLayout {

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

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    public PartiView(@Autowired AuthenticatedUser authenticatedUser,
                     @Autowired TripRepository tripRepository,
                     @Autowired TripParticipantsRepository tripParticipantsRepository,
                     @Autowired LocationRepository locationRepository,
                     @Autowired TripLocationRepository tripLocationRepository) {

        this.locationRepository = locationRepository;
        this.gridItems = new ArrayList<>();

        addClassName("parti-view");
        getStyle()
                .set("background-image", "url('images/background.png')");

        VerticalLayout container = new VerticalLayout();
        container.addClassNames("esplora-view", "main-container");

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

            UI.getCurrent().navigate(EsploraView.class);
        });

        cancel.addClickListener(click -> UI.getCurrent().navigate(EsploraView.class));

        add(container);
    }

    private Component createTitle() {
        return new H3("Crea un'escursione");
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

        H4 itinerari = new H4("Aggiungi itinerari");

        searchLocation = new TextField();
        searchLocation.setValueChangeMode(ValueChangeMode.EAGER);
        searchLocation.setPlaceholder("Cerca un itinerario");
        searchLocation.getStyle().set("padding", "0");

        Grid<Location> searchResultsGrid = new Grid<>();
        searchResultsGrid.setWidthFull();
        searchResultsGrid.setAllRowsVisible(true);
        searchResultsGrid.addColumn(Location::getName).setHeader("Fai doppio-click sull'itinerario da aggiungere");
        searchResultsGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        searchResultsGrid.addItemDoubleClickListener(click -> {
            Location clickItem = click.getItem();

            gridItems.add(clickItem);
            locationGrid.setItems(gridItems);

            map.setViewPoint(MapUtils.getCenteredViewpoint(gridItems.toArray(new Location[0])));

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

        map.setSizeFull();

        VerticalLayout mapContainer = new VerticalLayout(map);
        mapContainer.setSizeFull();
        mapContainer.setPadding(false);
        mapContainer.setMinHeight("400px");
        mapContainer.setHeight("0px");

        locationGrid = new Grid<>();
        locationGrid.setSizeFull();
        locationGrid.addColumn(Location::getName).setHeader("Itinerari");
        locationGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        locationGrid.addComponentColumn(location -> {

            Button delete = new Button(new Icon(VaadinIcon.TRASH));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            delete.addClickListener(click -> {

                gridItems.remove(location);
                locationGrid.setItems(gridItems);

                map.setViewPoint(MapUtils.getCenteredViewpoint(gridItems.toArray(new Location[0])));

            });

            return delete;
        }).setFlexGrow(0);
        locationGrid.setMinHeight("400px");

        formLayout.add(itinerari, 2);
        formLayout.add(searchLocation, 2);
        formLayout.add(searchResultsGrid, 2);
        formLayout.add(mapContainer, 1);
        formLayout.add(locationGrid, 1);

        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        buttonLayout.add(cancel);
        return buttonLayout;
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
