package it.trekkete.hikehunter.map;

import com.vaadin.flow.component.ClientCallable;
import it.trekkete.hikehunter.data.entity.Location;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.xdev.vaadin.maps.leaflet.flow.data.LCenter;
import software.xdev.vaadin.maps.leaflet.flow.data.LPoint;
import software.xdev.vaadin.maps.leaflet.flow.data.LTileLayer;

import java.util.HashMap;
import java.util.Map;

public class LMap extends software.xdev.vaadin.maps.leaflet.flow.LMap {

    private final Logger log = LogManager.getLogger(LMap.class);

    private static final String CLIENT_OVERPASS_LAYER = "this.oplayer";
    private LMapClickable clickable;

    private final Map<String, LTileLayer> layers;

    public static class Locations {

        public static final LCenter ROME = new LCenter(41.902782, 12.496366, 10);
    }

    public static class Layers {

        public static final LTileLayer DEFAULT_OPENSTREETMAP = LTileLayer.DEFAULT_OPENSTREETMAP_TILE;
        public static final LTileLayer STADIA_ALIDADE_SMOOTH_DARK = new LTileLayer("https://tiles.stadiamaps.com/tiles/alidade_smooth_dark/{z}/{x}/{y}{r}.png", "&copy; <a href=\"https://www.stadiamaps.com/\" target=\"_blank\">Stadia Maps</a> &copy; <a href=\"https://openmaptiles.org/\" target=\"_blank\">OpenMapTiles</a> &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors", 18, "ALIDADE");
        public static final LTileLayer WAYMARKEDTRAILS_HIKING = new LTileLayer("https://tile.waymarkedtrails.org/hiking/{z}/{x}/{y}.png", "Map data: &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors | Map style: &copy; <a href=\"https://waymarkedtrails.org\">waymarkedtrails.org</a> (<a href=\"https://creativecommons.org/licenses/by-sa/3.0/\">CC-BY-SA</a>)", 18, "WMTH");
        public static final LTileLayer WAYMARKEDTRAILS_CYCLING = new LTileLayer("https://tile.waymarkedtrails.org/cycling/{z}/{x}/{y}.png", "Map data: &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors | Map style: &copy; <a href=\"https://waymarkedtrails.org\">waymarkedtrails.org</a> (<a href=\"https://creativecommons.org/licenses/by-sa/3.0/\">CC-BY-SA</a>)", 18, "WMTC");
        public static final LTileLayer WAYMARKEDTRAILS_SLOPES = new LTileLayer("https://tile.waymarkedtrails.org/slopes/{z}/{x}/{y}.png", "Map data: &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors | Map style: &copy; <a href=\"https://waymarkedtrails.org\">waymarkedtrails.org</a> (<a href=\"https://creativecommons.org/licenses/by-sa/3.0/\">CC-BY-SA</a>)", 18, "WMTS");

    }

    public LMap() {
        super();

        this.layers = new HashMap<>();
        this.clickable = null;
    }

    public LMap(LCenter center) {
        super(center.getLat(), center.getLon(), center.getZoom());

        this.layers = new HashMap<>();
        this.clickable = null;
    }

    public void toggleTileLayer(LTileLayer tileLayer) {

        String tileLayerVar = "this.tilelayer_" + tileLayer.getId();

        if (layers.containsKey(tileLayerVar)) {
            String removeTileLayerIfPresent = "if (" + tileLayerVar + ") {this.map.removeLayer(" + tileLayerVar + ");}";

            this.getElement().executeJs(removeTileLayerIfPresent);
            layers.remove(tileLayerVar);
        } else {
            String link = StringEscapeUtils.escapeEcmaScript(tileLayer.getLink());
            String attribution = StringEscapeUtils.escapeEcmaScript(tileLayer.getAttribution());
            String id = StringEscapeUtils.escapeEcmaScript(tileLayer.getId());

            String addTileLayer = tileLayerVar + " = L.tileLayer('" + link + "',{attribution: '" + attribution + "', maxZoom: " + tileLayer.getMaxZoom() + (tileLayer.getId() != null ? ", id: '" + id + "'" : "") + "}).addTo(this.map);";

            this.getElement().executeJs(addTileLayer);
            layers.put(tileLayerVar, tileLayer);
        }
    }

    public LOverpassLayer addOverpassLayer() {

        LOverpassLayer overpassLayer =
                new LOverpassLayer("https://overpass-api.de/api/interpreter");

        this.getElement().executeJs("if (" + CLIENT_OVERPASS_LAYER + ") {this.map.removeLayer(" + CLIENT_OVERPASS_LAYER + ");}");
        this.getElement().executeJs(CLIENT_OVERPASS_LAYER + "=new L.GeoJSON(null, {" +
                "pointToLayer: (feature, latlng) => {" +
                "if (feature.properties.radius) {" +
                "return new L.Circle(latlng, feature.properties.radius);" +
                "} else {" +
                "return new L.Marker(latlng);" +
                "}" +
                "}," +
                "onEachFeature: function(e, layer) {" +
                "if (e.properties && e.properties.name) layer.bindPopup(e.properties.name);" +
                "if (e.properties && e.properties.style) layer.setStyle(e.properties.style);" +
                "}" +
                "});");

        this.getElement().executeJs("this.map.addLayer(" + CLIENT_OVERPASS_LAYER + ");");

        return overpassLayer;
    }

    public void addData(JSONElement geojson) {
        this.getElement().executeJs(CLIENT_OVERPASS_LAYER + ".addData(" + geojson + ");");
    }

    public void clear() {
        this.getElement().executeJs(CLIENT_OVERPASS_LAYER + ".clearLayers();");
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

    public void addClickListener(LMapClickable clickable) {
        this.getElement().executeJs("this.map.on('click', function(e) { $0.$server.getClickEventCoordinates(e.latlng.lat,e.latlng.lng);});", getElement());

        this.clickable = clickable;
    }

    @ClientCallable
    public void getClickEventCoordinates(Double lat, Double lon) {

        log.trace("Clicked on map at lat: {}, lon: {}", lat, lon);

        if (clickable != null) {
            clickable.apply(lat, lon);
        }
    }
}
