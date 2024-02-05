package it.trekkete.hikehunter.map;

public class LGeoJSONProperties {

    private Long radius;
    private String color;
    private String weight;

    public LGeoJSONProperties() {
        this.radius = 60L;
        this.weight = "4";
        this.color = "#88aaff";
    }

    public LGeoJSONProperties(Long radius, String color, String weight) {
        this.radius = radius;
        this.color = color;
        this.weight = weight;
    }

    public Long getRadius() {
        return radius;
    }

    public void setRadius(Long radius) {
        this.radius = radius;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
