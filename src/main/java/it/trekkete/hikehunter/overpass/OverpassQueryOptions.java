package it.trekkete.hikehunter.overpass;

public class OverpassQueryOptions {

    public enum Format {
        JSON,
        XML
    }

    public enum Output {
        BODY,
        GEOM,
        SKEL,
        IDS,
        QT,
        META,
        TAGS
    }

    private Format format;
    private Output[] output;
    private Integer limit;

    private String maxSize;
    private String timeout;

    public OverpassQueryOptions() {
        this.output = new Output[]{ Output.BODY };
        this.timeout = "15";
        this.maxSize = "1073741824";
        this.format = Format.JSON;
        this.limit = null;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public Output[] getOutput() {
        return output;
    }

    public void setOutput(Output... output) {
        this.output = output;
    }

    public String getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
