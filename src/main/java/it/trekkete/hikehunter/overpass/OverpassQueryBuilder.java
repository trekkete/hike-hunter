package it.trekkete.hikehunter.overpass;

public class OverpassQueryBuilder {

    private OverpassQueryOptions options;
    private String query;

    public OverpassQueryBuilder() {
        options = new OverpassQueryOptions();
    }

    public OverpassQueryBuilder(OverpassQueryOptions options) {
        this.options = options;
    }

    public String build() {

        StringBuilder builder = new StringBuilder();

        if (options.getFormat() != null) {
            builder.append("[out:").append(options.getFormat().name().toLowerCase()).append("]");
        }
        else {
            throw new RuntimeException("Overpass API output format cannot be null");
        }

        if (options.getTimeout() != null) {
            builder.append("[timeout:").append(options.getTimeout()).append("]");
        }

        if (options.getMaxSize() != null) {
            builder.append("[maxsize:").append(options.getMaxSize()).append("]");
        }

        builder.append(";");

        if (query != null && !query.isEmpty()) {
            builder.append(query);
        }
        else {
            throw new RuntimeException("Overpass API query cannot be null");
        }

        if (options.getOutput() != null) {
            builder.append("out ").append(options.getOutput().name().toLowerCase());
        }
        else {
            throw new RuntimeException("Overpass API output format cannot be null");
        }

        builder.append(";");

        return builder.toString();

    }

    public OverpassQueryOptions getOptions() {
        return options;
    }

    public void setOptions(OverpassQueryOptions options) {
        this.options = options;
    }

    public OverpassQueryBuilder setFormat(OverpassQueryOptions.Format format) {
        options.setFormat(format);

        return this;
    }

    public OverpassQueryBuilder setOutput(OverpassQueryOptions.Output output) {
        options.setOutput(output);

        return this;
    }

    public OverpassQueryBuilder setTimeout(String timeout) {
        options.setTimeout(timeout);

        return this;
    }

    public OverpassQueryBuilder setMaxSize(String maxSize) {
        options.setMaxSize(maxSize);

        return this;
    }

    public OverpassQueryBuilder setQuery(String query) {
        this.query = query;

        return this;
    }
}
