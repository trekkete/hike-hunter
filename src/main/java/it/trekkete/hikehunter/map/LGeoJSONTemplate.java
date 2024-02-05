package it.trekkete.hikehunter.map;

import com.vaadin.flow.component.Component;
import kong.unirest.json.JSONElement;

public interface LGeoJSONTemplate {

    Component get(JSONElement element);
}
