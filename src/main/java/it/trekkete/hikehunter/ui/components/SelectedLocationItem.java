package it.trekkete.hikehunter.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.overpass.OverpassElementKeys;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;

import java.util.Map;

public class SelectedLocationItem extends HorizontalLayout {

    private final JSONObject element;
    private final Button button;

    public SelectedLocationItem(JSONElement element) {
        this.element = (JSONObject) element;

        button = new Button(FontAwesome.Solid.CLOSE.create());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
    }

    private void constructUI() {

        removeAll();

        setPadding(false);
        setSpacing(false);
        setWidthFull();
        addClassNames(LumoUtility.AlignItems.BASELINE, LumoUtility.JustifyContent.BETWEEN);

        Map<String, Object> tags = element.getJSONObject("tags").toMap();

        if (tags.containsKey(OverpassElementKeys.NAME)) {

            H5 title = new H5(String.valueOf(tags.get(OverpassElementKeys.NAME)));
            title.addClassNames(LumoUtility.Margin.NONE);

            add(title);
        }

        add(button);

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }

    public Button getButton() {
        return button;
    }
}
