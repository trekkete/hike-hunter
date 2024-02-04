package it.trekkete.hikehunter.map;

import kong.unirest.ContentType;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.xdev.vaadin.maps.leaflet.flow.data.LTileLayer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LOverpassLayer {

    private final Logger log = LogManager.getLogger(LOverpassLayer.class);

    public static final LTileLayer DEFAULT_OVERPASS_TILE = new LTileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", "© <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a>, POI via <a href=\"http://www.overpass-api.de/\">Overpass API</a>", 18);

    private final String endpoint;

    private Map<String, JSONElement> nodes;
    private Map<String, JSONElement> ways;

    private Map<String, JSONElement> results;

    public LOverpassLayer(String endpoint) {
        this.endpoint = endpoint;

        reset();
    }

    private void reset() {
        nodes = new HashMap<>();
        ways = new HashMap<>();
        results = new HashMap<>();
    }

    public JSONObject query(String query) {

        reset();

        String data = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        JsonNode body = Unirest.post(endpoint)
                .contentType(ContentType.APPLICATION_FORM_URLENCODED.toString())
                .body(data)
                .asJson().getBody();

        if (body != null) {
            JSONObject bodyObject = body.getObject();

            parseOverPassJSON(bodyObject);

            return bodyObject;
        }

        return null;
    }

    private void parseOverPassJSON(JSONObject results) {

        if (results == null || results.toString().isEmpty())
            return;

        if (results.has("elements")) {
            JSONArray elements = results.getJSONArray("elements");

            for (int i = 0; i < elements.length(); i++) {

                JSONObject element = elements.getJSONObject(i);

                if (element.has("type")) {

                    String type = element.getString("type");

                    switch (type) {
                        case "node":
                            parseNode(element, "id", null);

                            break;
                        case "way" :
                            parseWay(element, "id", null);
                            break;
                        case "relation" : {

                            if (!element.has("members") || element.getJSONArray("members").isEmpty()) {
                                break;
                            }

                            String name;
                            if (element.has("tags") || element.getJSONObject("tags").has("name")) {
                                name = element.getJSONObject("tags").getString("name");
                                element.put("name", name);
                            } else {
                                name = null;
                            }

                            JSONArray members = element.getJSONArray("members");
                            members.forEach(_mem -> {

                                JSONObject mem = (JSONObject) _mem;

                                if (mem.getString("type").equals("node")) {
                                    parseNode(mem, "ref", name);
                                }
                                else {
                                    parseWay(mem, "ref", name);
                                }
                            });

                        }
                        break;
                    }
                }

                this.results.put(element.getString("id"), element);
            }
        }
    }

    private void parseNode(JSONObject element, String idKey, String name) {
        if (!element.has("lat") || !element.has("lon"))
            return;

        Double lat = element.getDouble("lat");
        Double lon = element.getDouble("lon");

        JSONArray coordinates = new JSONArray();
        coordinates.put(lon);
        coordinates.put(lat);

        element.put("coordinates", coordinates);

        JSONObject geometry = new JSONObject();
        geometry.put("type", "Point");
        geometry.put("coordinates", coordinates);

        element.put("geometry", geometry);

        if (element.has("tags") && element.getJSONObject("tags").has("name"))
            element.put("name", element.getJSONObject("tags").getString("name"));
        else if (name != null){
            element.put("name", name);
        }

        if (!element.has("name"))
            log.trace(element);

        nodes.put(element.getString(idKey), element);
    }

    private void parseWay(JSONObject element, String idKey, String name) {

        if (!element.has("geometry"))
            return;

        JSONArray coordinatesList;
        try {
            coordinatesList = element.getJSONArray("geometry");
        } catch (Exception e) {
            log.trace("Could not find 'geometry' element for way: {}", element);

            return;
        }

        JSONArray coordinates = new JSONArray();
        coordinatesList.forEach(_element -> {

            JSONObject _coordinate = (JSONObject) _element;

            if (!_coordinate.has("lat") || !_coordinate.has("lon"))
                return;

            JSONArray coordinate = new JSONArray();
            coordinate.put(_coordinate.getDouble("lon"));
            coordinate.put(_coordinate.getDouble("lat"));

            coordinates.put(coordinate);
        });

        element.put("coordinates", coordinates);

        JSONObject geometry = new JSONObject();
        geometry.put("type", "LineString");
        geometry.put("coordinates", coordinates);

        element.put("geometry", geometry);

        if (element.has("tags") && element.getJSONObject("tags").has("name"))
            element.put("name", element.getJSONObject("tags").getString("name"));
        else if (name != null){
            element.put("name", name);
        }

        if (!element.has("name"))
            log.trace(element.getString(idKey));

        ways.put(element.getString(idKey), element);
    }

    public Map<String, JSONElement> getNodes() {
        return nodes;
    }

    public Map<String, JSONElement> getWays() {
        return ways;
    }

    public Map<String, JSONElement> getResults() {
        return results;
    }

    public JSONElement get(String id) {

        if (ways.containsKey(id)) {
            return ways.get(id);
        }

        if (nodes.containsKey(id)) {
            return nodes.get(id);
        }

        return null;
    }
}
