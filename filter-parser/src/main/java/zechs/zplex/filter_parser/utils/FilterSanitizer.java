package zechs.zplex.filter_parser.utils;

import zechs.zplex.filter_parser.model.Filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FilterSanitizer {

    private FilterSanitizer() {
    }

    public static void removeDuplicates(Filter filter) {
        Set<Integer> uniqueGenres = new HashSet<>(filter.getGenres());
        filter.setGenres(new ArrayList<>(uniqueGenres));

        Set<Integer> uniqueStudios = new HashSet<>(filter.getStudios());
        filter.setStudios(new ArrayList<>(uniqueStudios));

        Set<Integer> uniqueYears = new HashSet<>(filter.getYears());
        filter.setYears(new ArrayList<>(uniqueYears));

        Set<String> uniqueRatings = new HashSet<>(filter.getParentalRatings());
        filter.setParentalRatings(new ArrayList<>(uniqueRatings));
    }

}
