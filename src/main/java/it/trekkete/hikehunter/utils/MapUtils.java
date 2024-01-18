package it.trekkete.hikehunter.utils;

import it.trekkete.hikehunter.data.entity.Location;
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

    //TODO
    public static List<LMarker> route(Location... locations) {return null;}
}
