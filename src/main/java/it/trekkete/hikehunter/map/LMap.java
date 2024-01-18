package it.trekkete.hikehunter.map;

import it.trekkete.hikehunter.data.entity.Location;
import kong.unirest.json.JSONObject;
import software.xdev.vaadin.maps.leaflet.flow.data.LCenter;
import software.xdev.vaadin.maps.leaflet.flow.data.LPoint;

public class LMap extends software.xdev.vaadin.maps.leaflet.flow.LMap {

    private static final String CLIENT_OVERPASS_LAYER = "this.oplayer";

    public static class Locations {

        public static final LCenter ROME = new LCenter(41.902782, 12.496366, 10);
    }

    public LMap() {
        super();
    }

    public LMap(LCenter center) {
        super(center.getLat(), center.getLon(), center.getZoom());
    }

    public void setOverpassLayer(LOverpassLayer overpassLayer) {

        this.getElement().executeJs("if (" + CLIENT_OVERPASS_LAYER + ") {this.map.removeLayer(" + CLIENT_OVERPASS_LAYER + ");}");
        this.getElement().executeJs(CLIENT_OVERPASS_LAYER + "=new L.GeoJSON(null, {" +
                "onEachFeature: function(e, layer) {" +
                "if (e.properties && e.properties.name) " + CLIENT_OVERPASS_LAYER + ".bindPopup(e.properties.name);" +
                "if (e.properties && e.properties.style) " + CLIENT_OVERPASS_LAYER + ".setStyle(e.properties.style);" +
                "}" +
                "});");
        this.getElement().executeJs("this.map.addLayer(" + CLIENT_OVERPASS_LAYER + ");");

        overpassLayer.getWays().forEach((k, w) -> {

            JSONObject way = (JSONObject) w;

            JSONObject geo = new JSONObject();
            geo.put("type", "Feature");
            geo.put("geometry", way.get("geometry"));

            JSONObject properties = new JSONObject();
            properties.put("name", String.valueOf(way.get("id")));

            JSONObject style = new JSONObject();
            style.put("color", "#00FF00");
            style.put("weight", "3");

            properties.put("style", style);

            geo.put("properties", properties);

            this.getElement().executeJs(CLIENT_OVERPASS_LAYER + ".addData(" + geo + ");");

        });
    }

    public void fitBounds(Location... locations) {

        double minLat = 90, maxLat = -90, minLon = 180, maxLon = -180;

        for (int i = 0; i < locations.length; i++) {

            Location loc = locations[i];

            if (loc.getLatitude() < minLat)
                minLat = loc.getLatitude();

            if (loc.getLatitude() > maxLat)
                maxLat = loc.getLatitude();

            if (loc.getLongitude() < minLon)
                minLon = loc.getLongitude();

            if (loc.getLongitude() > maxLon)
                maxLon = loc.getLongitude();
        }

        this.centerAndZoom(new LPoint(minLat, minLon), new LPoint(maxLat, maxLon));
    }
}
