package it.trekkete.hikehunter.ui.window;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.map.LMap;
import software.xdev.vaadin.maps.leaflet.flow.data.LTileLayer;

public class LayersToggleWindow extends VerticalLayout {

    private final LMap map;

    public LayersToggleWindow(LMap map) {
        super();
        this.map = map;

        HorizontalLayout container = new HorizontalLayout(
                buildLayerComponent("Sentieri", LMap.Layers.WAYMARKEDTRAILS_HIKING, FontAwesome.Solid.HIKING.create()),
                buildLayerComponent("Ciclopercorsi", LMap.Layers.WAYMARKEDTRAILS_CYCLING, FontAwesome.Solid.BICYCLE.create()),
                buildLayerComponent("Piste da sci", LMap.Layers.WAYMARKEDTRAILS_SLOPES, FontAwesome.Solid.SKIING.create())
        );
        container.setWidthFull();
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);

        add(container);
    }

    private VerticalLayout buildLayerComponent(String label, LTileLayer layer, Icon icon) {

        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setSpacing(false);
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        icon.addClassNames(LumoUtility.IconSize.LARGE);
        Button button = new Button(icon, click -> {
            map.toggleTileLayer(layer);
        });
        button.getStyle()
                .set("background", "white")
                .set("box-shadow", "0 0 .1em gray")
                .set("border-radius", "1em");
        button.setWidth("80px");
        button.setHeight("80px");

        container.add(button);

        Span title = new Span(label);
        title.getStyle().set("font-weight", "bolder");

        container.add(title);

        return container;
    }
}
