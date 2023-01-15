package it.trekkete.hikehunter.ui.components;

import software.xdev.vaadin.maps.leaflet.flow.data.LIcon;
import software.xdev.vaadin.maps.leaflet.flow.data.LMarker;

public class CustomMarker extends LMarker {

    public CustomMarker(double lat, double lon, String color) {
        super(lat, lon);

        setColor(color);
    }

    public void setColor(String color) {
        LIcon icon = new LIcon(
                "data:image/svg+xml;utf8,<svg width=\"800px\" height=\"800px\" viewBox=\"-5.4 -5.4 46.80 46.80\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" fill=\"%23000000\" stroke=\"%23000000\" transform=\"rotate(0)\">\n" +
                        "<g id=\"SVGRepo_bgCarrier\" stroke-width=\"0\"/>\n" +
                        "<g id=\"SVGRepo_tracerCarrier\" stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke=\"%23CCCCCC\" stroke-width=\"0.072\"/>\n" +
                        "<g id=\"SVGRepo_iconCarrier\"><title>map-marker</title> <desc>Created with Sketch.</desc> <defs> </defs> <g id=\"Vivid.JS\" stroke-width=\"0.36\" fill=\"none\" fill-rule=\"evenodd\"> <g id=\"Vivid-Icons\" transform=\"translate(-125.000000, -643.000000)\"> <g id=\"Icons\" transform=\"translate(37.000000, 169.000000)\"> <g id=\"map-marker\" transform=\"translate(78.000000, 468.000000)\"> <g transform=\"translate(10.000000, 6.000000)\"> <path d=\"M14,0 C21.732,0 28,5.641 28,12.6 C28,23.963 14,36 14,36 C14,36 0,24.064 0,12.6 C0,5.641 6.268,0 14,0 Z\" id=\"Shape\" fill=\"" + color + "\"> </path> <circle id=\"Oval\" fill=\"%23ffffff\" fill-rule=\"nonzero\" cx=\"14\" cy=\"14\" r=\"7\"> </circle> </g> </g> </g> </g> </g> </g>\n" +
                        "</svg>"
        );

        icon.setIconSize(100, 40);
        icon.setIconAnchor(50, 20);

        setIcon(icon);

    }
}
