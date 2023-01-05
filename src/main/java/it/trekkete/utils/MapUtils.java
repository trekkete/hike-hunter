package it.trekkete.utils;

import it.trekkete.data.entity.Location;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import software.xdev.vaadin.maps.leaflet.flow.data.LCenter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
            location.setName(locationObj.getString("display_name"));
            location.setLatitude(locationObj.getDouble("lat"));
            location.setLongitude(locationObj.getDouble("lon"));

            locations.add(location);
        }

        return locations;
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
}
