package it.trekkete.hikehunter.utils;

public class OverpassLayer {

    private boolean debug;
    private String endpoint;
    private String query;
    private Integer minZoom;

    public OverpassLayer(boolean debug, String endpoint, String query, Integer minZoom) {
        this.debug = debug;
        this.endpoint = endpoint;
        this.query = query;
        this.minZoom = minZoom;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
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

    public Integer getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(Integer minZoom) {
        this.minZoom = minZoom;
    }
}
