package it.trekkete.hikehunter.ui.views.general;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import it.trekkete.hikehunter.map.LOverpassLayer;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.map.LMap;

@PageTitle("Map")
@Route(value = "map", layout = MainLayout.class)
@AnonymousAllowed
public class MapView extends VerticalLayout {

    public MapView() {

        constructUI();
    }

    private void constructUI() {

        setPadding(false);
        setSizeFull();
        setSpacing(false);

        LMap map = new LMap(LMap.Locations.ROME);
        map.setTileLayer(LOverpassLayer.DEFAULT_OVERPASS_TILE);
        map.getElement().executeJs("this.map.options.minZoom = 6;");
        map.setWidthFull();
        map.setMinHeight("100%");
        map.setHeight("900px");
        map.setOverpassLayer(new LOverpassLayer("https://overpass-api.de/api/interpreter", "[out:json][timeout:25];nwr[\"to\"=\"Rifugio Vioz \\\"Mantova\\\"\"];(._;>;);out qt;"));

        add(map);
    }


}
