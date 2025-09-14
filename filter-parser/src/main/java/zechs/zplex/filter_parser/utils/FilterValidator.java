package zechs.zplex.filter_parser.utils;

import zechs.zplex.config.model.FilterConfig;
import zechs.zplex.config.model.IdNamePair;
import zechs.zplex.filter_parser.exception.InvalidFilterException;
import zechs.zplex.filter_parser.model.Filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterValidator {

    private FilterValidator() {
        // Utility class
    }

    public static void validateFilters(Filter filter, FilterConfig filterConfig) {
        validateFilter(filter.getGenres(), filterConfig.getGenres().stream().map(IdNamePair::getId).toList(), "genres");
        validateFilter(filter.getStudios(), filterConfig.getStudios().stream().map(IdNamePair::getId).toList(), "studios");
        validateFilter(filter.getYears(), filterConfig.getYears(), "years");
        validateFilter(
                filter.getParentalRatings().stream().map(String::toLowerCase).collect(Collectors.toList()),
                filterConfig.getParentalRatings().stream().map(String::toLowerCase).collect(Collectors.toList()),
                "parental ratings"
        );
    }

    private static <T> void validateFilter(Collection<T> filterItems, Collection<T> configItems, String filterType) {
        Set<T> configSet = new HashSet<>(configItems);
        for (T item : filterItems) {
            if (!configSet.contains(item)) {
                throw new InvalidFilterException("Invalid " + filterType + ": " + item);
            }
        }
    }

}
