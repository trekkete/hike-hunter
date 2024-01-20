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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.data.entity.Location;
import it.trekkete.hikehunter.map.LMap;
import it.trekkete.hikehunter.map.LOverpassLayer;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.ui.views.logged.CreateTripView;
import it.trekkete.hikehunter.ui.window.LayersToggleWindow;
import it.trekkete.hikehunter.utils.AppEvents;
import org.jboss.jandex.Main;
import software.xdev.vaadin.maps.leaflet.flow.data.LCenter;

import javax.swing.text.html.Option;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;

@PageTitle("Map")
@Route(value = "map", layout = MainLayout.class)
@AnonymousAllowed
public class MapView extends VerticalLayout implements PropertyChangeListener {

    private LMap map;

    public MapView() {}

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        MainLayout.getCurrentLayout().ifPresent(mainLayout -> mainLayout.addChangeListener(this));

        constructUI();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        MainLayout.getCurrentLayout().ifPresent(mainLayout -> mainLayout.removeChangeListener(this));
    }

    private void constructUI() {

        setPadding(false);
        setSizeFull();
        setSpacing(false);
        addClassNames(LumoUtility.Position.RELATIVE);

        map = new LMap(LMap.Locations.ROME);
        map.toggleTileLayer(LMap.Layers.DEFAULT_OPENSTREETMAP);
        //map.toggleTileLayer(LMap.Layers.WAYMARKEDTRAILS_HIKING);
        map.getElement().executeJs("this.map.options.minZoom = 6;");
        map.setWidthFull();
        map.setMinHeight("100%");
        map.setHeight("900px");
        map.setOverpassLayer(new LOverpassLayer("https://overpass-api.de/api/interpreter", "[out:json][timeout:25];nwr[\"to\"=\"Rifugio Vioz \\\"Mantova\\\"\"];(._;>;);out qt;"));

        add(map);

        Button toggle = new Button(FontAwesome.Solid.LAYER_GROUP.create());
        toggle.setWidth("30px");
        toggle.getStyle()
                .set("box-shadow", "0 0 .1em gray")
                .set("border-radius", "50%")
                .set("background", "white");
        toggle.addClickListener(click -> {
            new LayersToggleWindow(map).open();
        });

        Icon plus = FontAwesome.Solid.PLUS.create();
        plus.addClassNames(LumoUtility.IconSize.LARGE);
        Button create = new Button(plus);
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        create.setWidth("55px");
        create.setHeight("55px");
        create.getStyle().set("box-shadow", "0 0 .1em gray")
                .set("border-radius", "1em");

        create.addClickListener(click -> {
            UI.getCurrent().navigate(CreateTripView.class);
        });

        Icon bullseye = FontAwesome.Solid.LOCATION_CROSSHAIRS.create();
        bullseye.addClassNames(LumoUtility.IconSize.LARGE);
        Button here = new Button(bullseye);
        here.setWidth("55px");
        here.setHeight("55px");
        here.getStyle()
                .set("background", "white")
                .set("border-radius", "50%")
                .set("box-shadow", "0 0 .1em gray");

        here.addClickListener(click -> MainLayout.triggerUserLocation().ifPresent(this::update));

        VerticalLayout bottomButtonsContainer = new VerticalLayout(here, create);
        bottomButtonsContainer.setPadding(false);
        bottomButtonsContainer.setSpacing(false);
        bottomButtonsContainer.setSizeUndefined();

        VerticalLayout buttons = new VerticalLayout(toggle, bottomButtonsContainer);
        buttons.setPadding(false);
        buttons.setSpacing(false);
        buttons.setSizeUndefined();
        buttons.addClassNames(
                LumoUtility.Position.ABSOLUTE,
                LumoUtility.AlignItems.END,
                LumoUtility.JustifyContent.BETWEEN);
        buttons.setHeight("93%");
        buttons.getStyle()
                .set("bottom", "40px")
                .set("right", "10px")
                .set("z-index", "99");

        add(buttons);

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
