package zechs.zplex.filter_parser.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zechs.zplex.filter_parser.model.Filter;
import zechs.zplex.filter_parser.model.enums.Playstate;
import zechs.zplex.filter_parser.model.enums.Status;

public class FilterBuilderTest {

    @Test
    public void testFiltersWithQuotedValues() {
        String expected = "playstate(played), genres(1234, 567), parentalRatings(\"R\", \"Unrated\")," +
                          " studios(890), years(2020, 2021), status(any)";

        Filter.Builder result = new Filter.Builder()
                .setPlaystate(Playstate.PLAYED)
                .addGenre(1234).addGenre(567)
                .addParentRating("R").addParentRating("Unrated")
                .addStudio(890)
                .addYear(2020).addYear(2021)
                .setStatus(Status.ANY)
                .quoteValuesInList(true);

        Assertions.assertEquals(expected, result.build());
    }

    @Test
    public void testFiltersWithUnquotedValues() {
        String expected = "playstate(played), genres(1234, 567), parentalRatings(R, Unrated)," +
                          " studios(890), years(2020, 2021), status(any)";

        String result = new Filter.Builder()
                .setPlaystate(Playstate.PLAYED)
                .addGenre(1234).addGenre(567)
                .addParentRating("R").addParentRating("Unrated")
                .addStudio(890)
                .addYear(2020).addYear(2021)
                .setStatus(Status.ANY)
                .build();

        Assertions.assertEquals(expected, result);
    }

    @Test
    public void testShortLengthValidFilters() {
        String expected = "genres(1234, 567), parentalRatings(\"R\", \"Unrated\")";
        String result = new Filter.Builder()
                .addGenre(1234).addGenre(567)
                .addParentRating("R").addParentRating("Unrated")
                .quoteValuesInList(true)
                .build();
        Assertions.assertEquals(expected, result);
    }

}