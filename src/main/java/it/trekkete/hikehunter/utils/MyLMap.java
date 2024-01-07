package it.trekkete.hikehunter.utils;

import software.xdev.vaadin.maps.leaflet.flow.LMap;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MyLMap extends LMap {

    public void setOverpassLayer(OverpassLayer overpassLayer) {

        String instr =
            "var opl = new L.OverPassLayer({" +
            "   debug: " + overpassLayer.isDebug() + "," +
            "   minZoom: " + overpassLayer.getMinZoom() + "," +
            "   endPoint: '" + overpassLayer.getEndpoint() + "'," +
            "   query: '" + URLEncoder.encode(overpassLayer.getQuery(), StandardCharsets.UTF_8) + "'," +
            "});" +
            "this.map.addLayer(opl); ";

        this.getElement().executeJs(instr);
    }
}
