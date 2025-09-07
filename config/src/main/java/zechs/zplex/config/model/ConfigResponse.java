package zechs.zplex.config.model;

import java.util.ArrayList;
import java.util.List;

public class ConfigResponse {
    private String serviceAccount;
    private List<FilterConfig> filters;

    public ConfigResponse() {
    }

    public ConfigResponse(String serviceAccount, List<FilterConfig> filters) {
        this.serviceAccount = serviceAccount;
        this.filters = filters;
    }

    public String getServiceAccount() {
        return serviceAccount;
    }

    public void setServiceAccount(String serviceAccount) {
        this.serviceAccount = serviceAccount;
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
