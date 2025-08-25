package zechs.zplex.filter_parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zechs.zplex.filter_parser.model.Filter;
import zechs.zplex.filter_parser.model.enums.Playstate;
import zechs.zplex.filter_parser.model.enums.Status;

import java.util.List;

class FilterExtractorTest {

    @Test
    public void testFullLengthValidQuotedFilters() {
        String filter = new Filter.Builder()
                .setPlaystate(Playstate.PLAYED)
                .addGenre(1234).addGenre(4567)
                .addParentRating("R").addParentRating("Unrated")
                .addStudio(890)
                .addYear(2020).addYear(2021)
                .setStatus(Status.ANY)
                .quoteValuesInList(true)
                .build();

        Filter result = FilterExtractor.parseFilters(filter);

        Assertions.assertEquals(Playstate.PLAYED, result.getPlaystate());
        Assertions.assertEquals(List.of(1234, 4567), result.getGenres());
        Assertions.assertEquals(List.of("r", "unrated"), result.getParentalRatings());
        Assertions.assertEquals(List.of(890), result.getStudios());
        Assertions.assertEquals(List.of(2020, 2021), result.getYears());
        Assertions.assertEquals(Status.ANY, result.getStatus());
    }

    @Test
    public void testFullLengthValidUnquotedFilters() {
        String filter = new Filter.Builder()
                .setPlaystate(Playstate.PLAYED)
                .addGenre(1234).addGenre(567)
                .addParentRating("R").addParentRating("Unrated")
                .addStudio(890)
                .addYear(2020).addYear(2021)
                .setStatus(Status.ANY)
                .build();

        Filter result = FilterExtractor.parseFilters(filter);

        Assertions.assertEquals(Playstate.PLAYED, result.getPlaystate());
        Assertions.assertEquals(List.of(1234, 567), result.getGenres());
        Assertions.assertEquals(List.of("r", "unrated"), result.getParentalRatings());
        Assertions.assertEquals(List.of(890), result.getStudios());
        Assertions.assertEquals(List.of(2020, 2021), result.getYears());
        Assertions.assertEquals(Status.ANY, result.getStatus());
    }

    @Test
    public void testFullLengthSaltedWithInvalidFilters() {
        String input = "playstate(unplayed), genres(1234 \"567\"), parenRating(\"R\", \"Unrated\")," +
                       " studios (\"890\"), years(2020, 2021), status(ended)";
        Filter result = FilterExtractor.parseFilters(input);

        Assertions.assertEquals(Playstate.UNPLAYED, result.getPlaystate());
        Assertions.assertEquals(List.of(1234), result.getGenres());
        Assertions.assertEquals(List.of(), result.getParentalRatings());
        Assertions.assertEquals(List.of(890), result.getStudios());
        Assertions.assertEquals(List.of(2020, 2021), result.getYears());
        Assertions.assertEquals(Status.ENDED, result.getStatus());
    }

    @Test
    public void testShortLengthValidFilters() {
        String input = "genres(\"1234\", \"5678\"), parentalRatings(\"R\", \"Unrated\")";
        Filter result = FilterExtractor.parseFilters(input);

        Assertions.assertNull(result.getPlaystate());
        Assertions.assertEquals(List.of(1234, 5678), result.getGenres());
        Assertions.assertEquals(List.of("r", "unrated"), result.getParentalRatings());
        Assertions.assertEquals(List.of(), result.getStudios());
        Assertions.assertEquals(List.of(), result.getYears());
        Assertions.assertNull(result.getStatus());
    }

    @Test
    public void testNumericFiltersOnly() {
        String input = "years(1999, 2005)";
        Filter result = FilterExtractor.parseFilters(input);

        Assertions.assertNull(result.getPlaystate());
        Assertions.assertEquals(List.of(1999, 2005), result.getYears());
        Assertions.assertEquals(List.of(), result.getGenres());
        Assertions.assertEquals(List.of(), result.getParentalRatings());
        Assertions.assertEquals(List.of(), result.getStudios());
        Assertions.assertNull(result.getStatus());
    }

    @Test
    public void testNoValidFilter() {
        String input = "just a random sequence of strings";
        Filter result = FilterExtractor.parseFilters(input);

        Assertions.assertNull(result.getPlaystate());
        Assertions.assertEquals(List.of(), result.getGenres());
        Assertions.assertEquals(List.of(), result.getParentalRatings());
        Assertions.assertEquals(List.of(), result.getStudios());
        Assertions.assertEquals(List.of(), result.getYears());
        Assertions.assertNull(result.getStatus());
    }

    @Test
    public void testDuplicateFilterValues() {
        String input = "genres(\"1234\", \"1234\", \"5678\"), parentalRatings(\"R\", \"R\")";
        Filter result = FilterExtractor.parseFilters(input);

        Assertions.assertNull(result.getPlaystate());
        Assertions.assertEquals(List.of(1234, 5678), result.getGenres());
        Assertions.assertEquals(List.of("r"), result.getParentalRatings());
        Assertions.assertEquals(List.of(), result.getStudios());
        Assertions.assertEquals(List.of(), result.getYears());
        Assertions.assertNull(result.getStatus());
    }

    @Test
    public void testFiltersWithWhitespaceVariations() {
        String input = " playstate ( played ) , genres (  \"1234\" , \"567\"  ) ,   studios ( \"890\" ) ";
        Filter result = FilterExtractor.parseFilters(input);

        Assertions.assertEquals(Playstate.PLAYED, result.getPlaystate());
        Assertions.assertEquals(List.of(1234, 567), result.getGenres());
        Assertions.assertEquals(List.of(890), result.getStudios());
        Assertions.assertEquals(List.of(), result.getYears());
        Assertions.assertNull(result.getStatus());
    }

    @Test
    public void testPartialIncompleteFilter() {
        String input = "genres(\"1234\", \"567\", parentalRating(\"R\"), studio(\"890\" years(2020)";
        Filter result = FilterExtractor.parseFilters(input);

        Assertions.assertNull(result.getPlaystate());
        Assertions.assertEquals(List.of(), result.getGenres());
        Assertions.assertEquals(List.of(), result.getParentalRatings());
        Assertions.assertEquals(List.of(), result.getStudios());
        Assertions.assertEquals(List.of(), result.getYears());
        Assertions.assertNull(result.getStatus());
    }

    @Test
    public void testEmptyFilterValues() {
        String input = "genres(), parentalRating(), studio(), years()";
        Filter result = FilterExtractor.parseFilters(input);

        Assertions.assertNull(result.getPlaystate());
        Assertions.assertEquals(List.of(), result.getGenres());
        Assertions.assertEquals(List.of(), result.getParentalRatings());
        Assertions.assertEquals(List.of(), result.getStudios());
        Assertions.assertEquals(List.of(), result.getYears());
        Assertions.assertNull(result.getStatus());
    }

    @Test
    public void testMixedCaseSensitivityInFilters() {
        String input = "genres(\"1234\", \"567\"), parentalRatings(\"r\", \"UNRATED\"), studios(\"890\")";
        Filter result = FilterExtractor.parseFilters(input);

        Assertions.assertNull(result.getPlaystate());
        Assertions.assertEquals(List.of(1234, 567), result.getGenres());
        Assertions.assertEquals(List.of("r", "unrated"), result.getParentalRatings());
        Assertions.assertEquals(List.of(890), result.getStudios());
        Assertions.assertEquals(List.of(), result.getYears());
        Assertions.assertNull(result.getStatus());
    }

    @Test
    public void testEmptyInput() {
        String input = "";
        Filter result = FilterExtractor.parseFilters(input);

        Assertions.assertNull(result.getPlaystate());
        Assertions.assertEquals(List.of(), result.getGenres());
        Assertions.assertEquals(List.of(), result.getParentalRatings());
        Assertions.assertEquals(List.of(), result.getStudios());
        Assertions.assertEquals(List.of(), result.getYears());
        Assertions.assertNull(result.getStatus());
    }
}