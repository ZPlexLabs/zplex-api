package zechs.zplex.config.model;

import java.util.ArrayList;
import java.util.List;

public class ConfigResponse {
    private String streamingHost;
    private List<FilterConfig> filters;

    public ConfigResponse() {
    }

    public ConfigResponse(String streamingHost, List<FilterConfig> filters) {
        this.streamingHost = streamingHost;
        this.filters = filters;
    }

    public String getStreamingHost() {
        return streamingHost;
    }

    public void setStreamingHost(String streamingHost) {
        this.streamingHost = streamingHost;
    }

    public List<FilterConfig> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterConfig> filters) {
        this.filters = filters;
    }

    public void addFilter(FilterConfig filter) {
        if (filters == null) {
            filters = new ArrayList<FilterConfig>();
        }
        filters.add(filter);
    }
}
