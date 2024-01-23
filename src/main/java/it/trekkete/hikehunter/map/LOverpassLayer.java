package it.trekkete.hikehunter.map;

import kong.unirest.ContentType;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;
import software.xdev.vaadin.maps.leaflet.flow.data.LTileLayer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LOverpassLayer {

    public static final LTileLayer DEFAULT_OVERPASS_TILE = new LTileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", "© <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a>, POI via <a href=\"http://www.overpass-api.de/\">Overpass API</a>", 18);

    private String endpoint;
    private String query;

    private Map<String, JSONElement> nodes;
    private Map<String, JSONElement> ways;

    public LOverpassLayer(String endpoint, String query) {

        this.endpoint = endpoint;
        this.query = query;

        nodes = new HashMap<>();
        ways = new HashMap<>();

        JSONObject json = query(endpoint, query);

        parseOverPassJSON(json);
    }

    private JSONObject query(String endpoint, String query) {

        String data = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        JsonNode body = Unirest.post(endpoint)
                .contentType(ContentType.APPLICATION_FORM_URLENCODED.toString())
                .body(data)
                .asJson().getBody();

        if (body != null) {
            return body.getObject();
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
                        case "node": {

                            if (!element.has("lat") || !element.has("lon"))
                                continue;

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

                            nodes.put(element.getString("id"), element);
                        }
                        case "way" : {

                            if (!element.has("nodes"))
                                continue;

                            JSONArray nodesList = element.getJSONArray("nodes");

                            JSONArray coordinates = new JSONArray();
                            nodesList.forEach(_id -> {

                                if (!nodes.containsKey(String.valueOf(_id)))
                                    return;

                                JSONObject node = (JSONObject) nodes.get(String.valueOf(_id));

                                coordinates.put(node.getJSONArray("coordinates"));
                            });

                            element.put("coordinates", coordinates);

                            JSONObject geometry = new JSONObject();
                            geometry.put("type", "LineString");
                            geometry.put("coordinates", coordinates);

                            element.put("geometry", geometry);

                            ways.put(element.getString("id"), element);

                        }
                        case "relation" : {

                            if (!element.has("members") || element.getJSONArray("members").isEmpty()) {
                                break;
                            }

                            JSONArray members = element.getJSONArray("members");
                            members.forEach(_mem -> {

                                JSONObject mem = (JSONObject) _mem;

                                if (mem.getString("type").equals("node")) {
                                    mem.put("obj", nodes.get(String.valueOf(mem.getLong("ref"))));
                                }
                                else {
                                    mem.put("obj", ways.get(String.valueOf(mem.getLong("ref"))));
                                }
                            });

                        }
                    }
                }
            }
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, JSONElement> getNodes() {
        return nodes;
    }

    public Map<String, JSONElement> getWays() {
        return ways;
    }

    public JSONElement get(String id) {

        if (nodes.containsKey(id)) {
            return nodes.get(id);
        }

        if (ways.containsKey(id)) {
            return ways.get(id);
        }

        return null;
    }
}
