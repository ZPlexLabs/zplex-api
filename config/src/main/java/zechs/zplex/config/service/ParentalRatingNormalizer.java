package zechs.zplex.config.service;

import org.springframework.stereotype.Service;
import zechs.zplex.config.model.RatingRank;
import zechs.zplex.config.model.RatingRankInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Maps raw OMDB `Rated` strings to an ordered maturity rank. Unknown/legacy-seal/NULL -> null (unrated bucket).
@Service
public class ParentalRatingNormalizer {

    private static final Map<String, RatingRank> ALIASES = buildAliases();
    private static final int MAX_DEFINED_RANK =
            Arrays.stream(RatingRank.values()).mapToInt(RatingRank::getRank).max().orElse(Integer.MAX_VALUE);

    // Seals/placeholders that carry no reliable maturity level -> treated as unrated (governed by allowUnrated).
    private static final Set<String> UNRATED_TOKENS = Set.of(
            "N/A", "NA", "NR", "NOT RATED", "UNRATED", "UR", "APPROVED", "PASSED", "TBD", "NONE", "OPEN");

    private static final Pattern AGE = Pattern.compile("(\\d{1,2})");

    public RatingRank normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String key = raw.trim().toUpperCase(Locale.ENGLISH);
        if (key.isEmpty()) {
            return null;
        }
        RatingRank direct = ALIASES.get(key);
        if (direct != null) {
            return direct;
        }
        if (UNRATED_TOKENS.contains(key)) {
            return null;
        }
        Matcher matcher = AGE.matcher(key);
        if (matcher.find()) {
            return fromAge(Integer.parseInt(matcher.group(1)));
        }
        return null;
    }

    public Integer rankOf(String raw) {
        RatingRank rating = normalize(raw);
        return rating == null ? null : rating.getRank();
    }

    // Distinct raw ratings within the ceiling; null = no ceiling (user clears the top rank).
    public String[] allowedRatings(Collection<String> distinctRatings, int maxRatingRank) {
        if (maxRatingRank >= MAX_DEFINED_RANK) {
            return null;
        }
        return distinctRatings.stream()
                .filter(rating -> {
                    Integer rank = rankOf(rating);
                    return rank != null && rank <= maxRatingRank;
                })
                .distinct()
                .toArray(String[]::new);
    }

    // Distinct raw ratings that carry no maturity level (governed by allowUnrated).
    public String[] unratedRatings(Collection<String> distinctRatings) {
        return distinctRatings.stream()
                .filter(rating -> rankOf(rating) == null)
                .distinct()
                .toArray(String[]::new);
    }

    public List<RatingRankInfo> getCatalog() {
        List<RatingRankInfo> catalog = new ArrayList<>();
        for (RatingRank rating : RatingRank.values()) {
            catalog.add(new RatingRankInfo(rating.getRank(), rating.getLabel()));
        }
        return catalog;
    }

    private static RatingRank fromAge(int age) {
        if (age <= 6) {
            return RatingRank.GENERAL;
        }
        if (age <= 10) {
            return RatingRank.GUIDANCE;
        }
        if (age <= 14) {
            return RatingRank.TEEN;
        }
        if (age <= 17) {
            return RatingRank.MATURE;
        }
        return RatingRank.ADULT;
    }

    private static Map<String, RatingRank> buildAliases() {
        Map<String, RatingRank> map = new HashMap<>();
        putAll(map, RatingRank.GENERAL, "G", "TV-G", "TV-Y", "U", "E", "AL");
        putAll(map, RatingRank.GUIDANCE, "PG", "TV-PG", "TV-Y7", "TV-Y7-FV", "GP", "M/PG", "E10+");
        putAll(map, RatingRank.TEEN, "PG-13", "PG13", "TV-14", "TV-13", "T", "M");
        putAll(map, RatingRank.MATURE, "R", "TV-MA");
        putAll(map, RatingRank.ADULT, "NC-17", "NC17", "X", "AO");
        return map;
    }

    private static void putAll(Map<String, RatingRank> map, RatingRank rank, String... aliases) {
        for (String alias : aliases) {
            map.put(alias, rank);
        }
    }
}
