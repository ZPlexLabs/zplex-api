package zechs.zplex.config.service;

import com.google.gson.internal.LinkedTreeMap;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.common.utils.SimpleCache;
import zechs.zplex.config.model.FilterConfig;
import zechs.zplex.config.model.IdNamePair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class FilterConfigService {

    private static final Logger LOGGER = Logger.getLogger(FilterConfigService.class.getName());

    private static final String FILTER_CONFIG_CACHE_KEY = "filterConfig";
    private final SimpleCache<String, FilterConfig> localCache;
    private final static int LOCAL_CACHE_TTL_IN_HOURS = 24;
    private final JedisPooled redis;

    public FilterConfigService(JedisPooled redis) {
        this.redis = redis;
        this.localCache = new SimpleCache<>();
    }

    public FilterConfig getFilterConfig(MediaType type) {
        String cacheKey = FILTER_CONFIG_CACHE_KEY + "_" + type.name();

        FilterConfig cachedConfig = localCache.get(cacheKey);
        if (cachedConfig != null) {
            LOGGER.info("Cache hit for key: " + cacheKey);
            return cachedConfig;
        }

        FilterConfig filterConfig = new FilterConfig();
        switch (type) {
            case SHOW -> {
                filterConfig.setType("show");
                filterConfig.setGenres(getIdNameList("commonShowGenres"));
                filterConfig.setStudios(getIdNameList("commonShowStudios"));
                filterConfig.setYears(mapToInt(getStringList("commonShowYears")));
                filterConfig.setParentalRatings(getStringList("commonShowParentalRatings"));
            }
            case MOVIE -> {
                filterConfig.setType("movie");
                filterConfig.setGenres(getIdNameList("commonMovieGenres"));
                filterConfig.setStudios(getIdNameList("commonMovieStudios"));
                filterConfig.setYears(mapToInt(getStringList("commonMovieYears")));
                filterConfig.setParentalRatings(getStringList("commonMovieParentalRatings"));
            }
            default -> {
                String msg = "Unexpected MediaType: " + type;
                LOGGER.severe(msg);
                throw new IllegalStateException(msg);
            }
        }

        localCache.put(cacheKey, filterConfig, LOCAL_CACHE_TTL_IN_HOURS, TimeUnit.HOURS);
        LOGGER.info(String.format("Filter config cached locally with key: %s for %d hours", cacheKey, LOCAL_CACHE_TTL_IN_HOURS));
        return filterConfig;
    }

    private List<IdNamePair> getIdNameList(String key) {
        Object json = redis.jsonGet(key);
        if (json == null) {
            LOGGER.warning("No data found in Redis for key: " + key);
            return List.of();
        }

        @SuppressWarnings("unchecked")
        ArrayList<LinkedTreeMap<String, Object>> jsonData = (ArrayList<LinkedTreeMap<String, Object>>) json;

        List<IdNamePair> result = jsonData.stream()
                .map(element -> {
                    int id = ((Number) element.get("id")).intValue();
                    String name = (String) element.get("name");
                    return new IdNamePair(id, name);
                })
                .sorted(Comparator.comparing(IdNamePair::getName))
                .collect(Collectors.toList());

        LOGGER.info("Fetched " + result.size() + " ID-Name pairs for key: " + key);
        return result;
    }

    private List<String> getStringList(String key) {
        LOGGER.info("Fetching string list from Redis for key: " + key);
        Set<String> cachedSet = new HashSet<>(redis.lrange(key, 0, -1));
        if (cachedSet.isEmpty()) {
            LOGGER.warning("No strings found in Redis list for key: " + key);
        } else {
            LOGGER.info("Fetched " + cachedSet.size() + " strings for key: " + key);
        }
        return cachedSet.stream().sorted().toList();
    }

    private List<Integer> mapToInt(List<String> stringList) {
        try {
            return stringList.stream()
                    .map(Integer::parseInt)
                    .toList();
        } catch (NumberFormatException e) {
            LOGGER.severe("Error parsing integers from list: " + stringList + " - " + e.getMessage());
            return List.of();
        }
    }
}
