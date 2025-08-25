package zechs.zplex.filter_parser.utils;

import zechs.zplex.filter_parser.model.enums.Playstate;
import zechs.zplex.filter_parser.model.enums.Status;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterStringBuilder {

    public static String buildFilterString(Playstate playstate, Set<Integer> genres, Set<String> parentalRatings,
                                           Set<Integer> studios, Set<Integer> years, Status status, Boolean quote) {
        StringBuilder filter = new StringBuilder();
        boolean hasPrevious = false;

        if (playstate != null) {
            filter.append("playstate(")
                    .append(playstate.name().toLowerCase(Locale.ENGLISH))
                    .append(")");
            hasPrevious = true;
        }
        if (!genres.isEmpty()) {
            if (hasPrevious) filter.append(", ");
            String genreString = genres.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            filter.append("genres(").append(genreString).append(")");
            hasPrevious = true;
        }
        if (!parentalRatings.isEmpty()) {
            if (hasPrevious) filter.append(", ");
            String parentalRatingsString = parentalRatings.stream()
                    .map(value -> quote ? "\"" + value + "\"" : value)
                    .collect(Collectors.joining(", "));
            filter.append("parentalRatings(").append(parentalRatingsString).append(")");
            hasPrevious = true;
        }
        if (!studios.isEmpty()) {
            if (hasPrevious) filter.append(", ");
            String studiosString = studios.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            filter.append("studios(").append(studiosString).append(")");
            hasPrevious = true;
        }
        if (!years.isEmpty()) {
            if (hasPrevious) filter.append(", ");
            String yearsString = years.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            filter.append("years(").append(yearsString).append(")");
            hasPrevious = true;
        }
        if (status != null) {
            if (hasPrevious) filter.append(", ");
            filter.append("status(").append(status.name().toLowerCase(Locale.ENGLISH)).append(")");
        }

        return filter.toString();
    }
}
