package it.trekkete.hikehunter.ui.views.general;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.data.entity.Location;
import it.trekkete.hikehunter.data.entity.Trip;
import it.trekkete.hikehunter.data.entity.TripLocation;
import it.trekkete.hikehunter.data.service.LocationRepository;
import it.trekkete.hikehunter.data.service.TripLocationRepository;
import it.trekkete.hikehunter.data.service.TripRepository;
import it.trekkete.hikehunter.data.service.TripService;
import it.trekkete.hikehunter.map.LMap;
import it.trekkete.hikehunter.map.LOverpassLayer;
import it.trekkete.hikehunter.overpass.OverpassQueryBuilder;
import it.trekkete.hikehunter.overpass.OverpassQueryOptions;
import it.trekkete.hikehunter.security.AuthenticatedUser;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.ui.views.logged.CreateTripView;
import it.trekkete.hikehunter.ui.window.LayersToggleWindow;
import it.trekkete.hikehunter.utils.AppEvents;
import it.trekkete.hikehunter.utils.MapUtils;
import kong.unirest.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import software.xdev.vaadin.maps.leaflet.flow.data.LCenter;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Random;

@PageTitle("Map")
@Route(value = "map", layout = MainLayout.class)
@AnonymousAllowed
public class MapView extends VerticalLayout implements PropertyChangeListener {

    private final Logger log = LogManager.getLogger(MapView.class);

    private LMap map;

    private final AuthenticatedUser authenticatedUser;
    private final TripService tripService;
    private final TripLocationRepository tripLocationRepository;
    private final LocationRepository locationRepository;

    public MapView(@Autowired AuthenticatedUser authenticatedUser,
                   @Autowired TripRepository tripRepository,
                   @Autowired LocationRepository locationRepository,
                   @Autowired TripLocationRepository tripLocationRepository) {
        this.authenticatedUser = authenticatedUser;
        this.tripService = new TripService(tripRepository);
        this.locationRepository = locationRepository;
        this.tripLocationRepository = tripLocationRepository;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        MainLayout.getCurrentLayout().ifPresent(mainLayout -> {
            mainLayout.addChangeListener(this);
            log.trace("Registered {} to change listener", this.getClass().getSimpleName());
        });

        constructUI();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        MainLayout.getCurrentLayout().ifPresent(mainLayout -> {
            mainLayout.removeChangeListener(this);
            log.trace("Unregistered {} to change listener", this.getClass().getSimpleName());
        });
    }

    private void constructUI() {

        setPadding(false);
        setSizeFull();
        setSpacing(false);
        addClassNames(LumoUtility.Position.RELATIVE);

        List<Trip> available = tripService.findAllAvailable(50);

        StringBuilder sb = new StringBuilder();
        available.forEach(trip -> {

            List<TripLocation> locations = tripLocationRepository.findAllByTripOrderByIndex(trip.getId());

            locations.forEach(location -> {
                sb.append("nwr(").append(location.getLocation()).append(");");
            });
        });

        String query = new OverpassQueryBuilder()
                .setQuery("(" + sb + ");(._; >;);")
                .setOutput(OverpassQueryOptions.Output.QT)
                .build();

        LOverpassLayer overpassLayer =
                new LOverpassLayer("https://overpass-api.de/api/interpreter", query);

        map = new LMap(LMap.Locations.ROME);
        map.toggleTileLayer(LMap.Layers.DEFAULT_OPENSTREETMAP);
        //map.toggleTileLayer(LMap.Layers.WAYMARKEDTRAILS_HIKING);
        map.getElement().executeJs("this.map.options.minZoom = 6;");
        map.setWidthFull();
        map.setMinHeight("100%");
        map.setHeight("900px");
        map.setOverpassLayer(overpassLayer);

        available.forEach(trip -> {

            List<TripLocation> locations = tripLocationRepository.findAllByTripOrderByIndex(trip.getId());

            String color = Integer.toHexString(Color.getHSBColor(new Random().nextFloat(0.5f, 1.0f), 1.0f, 0.36f).getRGB()).substring(2);

            if (locations.size() > 1) {
                map.addData(MapUtils.tripToGeoJson(locations.stream().map(tripLocation -> locationRepository.findLocationById(tripLocation.getLocation())).toList(), trip.getTitle(), trip.getId().toString(), color));
            }

            locations.forEach(location -> {

                JSONObject element = (JSONObject) overpassLayer.get(location.getLocation());

                map.addData(MapUtils.elementToGeoJson(element, trip.getTitle(), location.getTrip().toString(), color));
            });
        });

        add(map);

        Button toggle = new Button(FontAwesome.Solid.LAYER_GROUP.create());
        toggle.setWidth("30px");
        toggle.addClassNames(LumoUtility.Position.ABSOLUTE);
        toggle.getStyle()
                .set("border", "2px solid rgba(0,0,0,.2)")
                .set("background-clip", "padding-box")
                .set("border-radius", "50%")
                .set("background-color", "white")
                .set("top", "8px")
                .set("right", "10px")
                .set("z-index", "99");;
        toggle.addClickListener(click -> {
            new LayersToggleWindow(map).open();
        });

        Icon plus = FontAwesome.Solid.PLUS.create();
        plus.addClassNames(LumoUtility.IconSize.LARGE);
        Button create = new Button(plus);
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        create.setWidth("55px");
        create.setHeight("55px");
        create.getStyle().set("border", "2px solid rgba(0,0,0,.2)")
                .set("background-clip", "padding-box")
                .set("border-radius", "1em");

        create.addClickListener(click -> {

            if (authenticatedUser.get().isPresent())
                return;

            VaadinSession.getCurrent().getSession().setAttribute(AppEvents.REROUTING_NEW_TRIP, "true");
            log.trace("Saving '{}' in session", AppEvents.REROUTING_NEW_TRIP);

            UI.getCurrent().navigate(CreateTripView.class);
        });

        Icon bullseye = FontAwesome.Solid.LOCATION_CROSSHAIRS.create();
        bullseye.addClassNames(LumoUtility.IconSize.LARGE);
        Button here = new Button(bullseye);
        here.setWidth("55px");
        here.setHeight("55px");
        here.getStyle()
                .set("background-color", "white")
                .set("border-radius", "50%")
                .set("border", "2px solid rgba(0,0,0,.2)")
                .set("background-clip", "padding-box");

        here.addClickListener(click -> MainLayout.triggerUserLocation().ifPresent(this::update));

        VerticalLayout bottomButtonsContainer = new VerticalLayout(here, create);
        bottomButtonsContainer.setPadding(false);
        bottomButtonsContainer.setSpacing(false);
        bottomButtonsContainer.setSizeUndefined();
        bottomButtonsContainer.addClassNames(LumoUtility.Position.ABSOLUTE);
        bottomButtonsContainer.getStyle()
                .set("bottom", "40px")
                .set("right", "10px")
                .set("z-index", "99");

        add(toggle, bottomButtonsContainer);

        MainLayout.triggerUserLocation().ifPresent(this::update);
    }


    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

        if (propertyChangeEvent.getPropertyName().equals(AppEvents.LOCATION_UPDATE)) {
            update((Location) propertyChangeEvent.getNewValue());
        }
    }

    private void update(Location userLocation) {
        map.setViewPoint(new LCenter(userLocation.getLatitude(), userLocation.getLongitude(), 10));
    }
}
