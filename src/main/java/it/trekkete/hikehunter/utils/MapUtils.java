package it.trekkete.hikehunter.utils;

import it.trekkete.hikehunter.data.entity.Location;
import it.trekkete.hikehunter.data.entity.TripLocation;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;
import software.xdev.vaadin.maps.leaflet.flow.data.LCenter;
import software.xdev.vaadin.maps.leaflet.flow.data.LMarker;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {

    public static LCenter getCoordinates(String address) {

        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);

        JSONArray results = Unirest.get("https://nominatim.openstreetmap.org/search?format=json&countrycodes=it&dedupe=1&q=" + encodedAddress)
                .asJson().getBody().getArray();

        JSONObject first = results.getJSONObject(0);
        LCenter center = new LCenter(first.getDouble("lat"), first.getDouble("lon"), 18);

        return center;
    }

    public static List<Location> getBestMatch(String address, int limit) {

        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);

        JSONArray results = Unirest.get("https://nominatim.openstreetmap.org/search?format=json&countrycodes=it&dedupe=1&q=" + encodedAddress)
                .asJson().getBody().getArray();

        List<Location> locations = new ArrayList<>();

        for (int i = 0; i < Math.min(limit, results.length()); i++) {

            Location location = new Location();

            JSONObject locationObj = results.getJSONObject(i);
            location.setId(locationObj.getString("osm_id"));
            location.setName(locationObj.getString("display_name"));
            location.setLatitude(locationObj.getDouble("lat"));
            location.setLongitude(locationObj.getDouble("lon"));

            locations.add(location);
        }

        return locations;
    }

    public static Location getBestMatch(double latitude, double longitude) {

        JSONObject result = Unirest.post("https://nominatim.openstreetmap.org/reverse?lat=" + latitude + "&lon=" + longitude + "&format=jsonv2")
                .asJson().getBody().getObject();

        Location location = new Location();
        location.setId(result.getString("osm_id"));
        location.setName(result.getString("display_name"));
        location.setLatitude(result.getDouble("lat"));
        location.setLongitude(result.getDouble("lon"));

        return location;
    }

    public static List<Location> getBestMatch(String address) {
        return getBestMatch(address, 5);
    }

    public static LCenter getCenteredViewpoint(Location... locations) {

        if (locations.length == 0)
            return new LCenter(45, 10, 7);

        double sumLat = 0, sumLon = 0;

        for (int i = 0; i < locations.length; i++) {
            sumLat += locations[i].getLatitude();
            sumLon += locations[i].getLongitude();
        }

        sumLat /= locations.length;
        sumLon /= locations.length;

        return new LCenter(sumLat, sumLon, 14);
    }

    public static JSONObject elementToGeoJson(JSONObject source, String title, String dest, String color) {

        JSONObject geo = new JSONObject();
        geo.put("type", "Feature");
        geo.put("geometry", source.get("geometry"));

        JSONObject properties = new JSONObject();
        properties.put("name", "<div style=\"display: flex; flex-direction: column;\"><div style=\"font-weight: bold; display: flex;\"><span style=\"text-align: center;\">" + title + "</span></div><a href=\"/trip/" + dest + "\">Vedi l'escursione</a></div>");

        JSONObject style = new JSONObject();
        style.put("color", "#" + color);
        style.put("weight", "4");

        properties.put("style", style);

        properties.put("radius", 40);

        geo.put("properties", properties);

        return geo;
    }

    public static JSONObject tripToGeoJson(List<Location> tripLocations, String title, String dest, String color) {

        JSONObject geo = new JSONObject();
        geo.put("type", "Feature");

        JSONArray coordinates = new JSONArray();
        tripLocations.forEach(location -> {

            JSONArray node = new JSONArray();
            node.put(location.getLongitude());
            node.put(location.getLatitude());

            coordinates.put(node);
        });

        JSONObject geometry = new JSONObject();
        geometry.put("type", "LineString");
        geometry.put("coordinates", coordinates);

        geo.put("geometry", geometry);

        JSONObject properties = new JSONObject();
        properties.put("name", "<div style=\"display: flex; flex-direction: column;\"><div style=\"font-weight: bold; display: flex;\"><span style=\"text-align: center;\">" + title + "</span></div><a href=\"/trip/" + dest + "\">Vedi l'escursione</a></div>");

        JSONObject style = new JSONObject();
        style.put("color", "#" + color);
        style.put("weight", "4");

        properties.put("style", style);

        properties.put("radius", 30);

        geo.put("properties", properties);

        return geo;
    }
}
