package it.trekkete.hikehunter.ui.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.trekkete.hikehunter.overpass.OverpassElementKeys;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;

import java.util.Map;

public class QueryResultItem extends VerticalLayout {

    private final JSONObject element;

    public QueryResultItem(JSONElement element) {
        this.element = (JSONObject) element;
    }

    private void constructUI() {

        removeAll();

        setPadding(false);
        setSpacing(false);
        setMinWidth("300px");

        Map<String, Object> tags = element.getJSONObject("tags").toMap();

        if (tags.containsKey(OverpassElementKeys.NAME)) {

            H5 title = new H5(String.valueOf(tags.get(OverpassElementKeys.NAME)));
            title.addClassNames(LumoUtility.Margin.NONE);

            add(title);
        }

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        constructUI();
    }
}
