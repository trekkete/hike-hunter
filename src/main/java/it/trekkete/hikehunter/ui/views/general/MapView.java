package it.trekkete.hikehunter.ui.views.general;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import it.trekkete.hikehunter.ui.views.MainLayout;
import it.trekkete.hikehunter.utils.MyLMap;
import software.xdev.vaadin.maps.leaflet.flow.data.LTileLayer;

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

        MyLMap map = new MyLMap();
        map.setTileLayer(LTileLayer.DEFAULT_OPENSTREETMAP_TILE);
        map.getElement().executeJs("this.map.options.minZoom = 6;");
        map.setWidthFull();
        map.setMinHeight("100%");
        map.setHeight("900px");

        add(map);
    }


}
