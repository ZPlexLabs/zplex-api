package zechs.zplex.filter_parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import zechs.zplex.filter_parser.model.Filter;
import zechs.zplex.filter_parser.model.enums.Playstate;
import zechs.zplex.filter_parser.model.enums.Status;
import zechs.zplex.zplex_api.FiltersBaseListener;
import zechs.zplex.zplex_api.FiltersLexer;
import zechs.zplex.zplex_api.FiltersParser;

import java.util.List;
import java.util.Locale;

public class FilterExtractor {

    public static Filter parseFilters(String input) {
        Filter result = new Filter();

        if (!areParenthesesValid(input)) {
            result.setPlaystate(null);
            result.setGenres(List.of());
            result.setStudios(List.of());
            result.setParentalRatings(List.of());
            result.setYears(List.of());
            result.setStatus(null);
            return result;
        }

        CharStream charStream = CharStreams.fromString(input.trim().toLowerCase(Locale.ENGLISH));
        FiltersLexer lexer = new FiltersLexer(charStream);
        lexer.removeErrorListeners(); // MUST REMOVE WHEN DEBUGGING

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FiltersParser parser = new FiltersParser(tokens);
        parser.removeErrorListeners(); // MUST REMOVE WHEN DEBUGGING

        ParseTree tree = parser.filters();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new FilterListener(result), tree);

        return result;
    }

    /**
     * Checks if the input has balanced and non-nested parentheses.
     * Returns true if the parentheses are valid, false otherwise.
     */
    private static boolean areParenthesesValid(String input) {
        int openParenCount = 0;

        for (char ch : input.toCharArray()) {
            if (ch == '(') {
                openParenCount++;
                if (openParenCount > 1) {
                    return false;
                }
            } else if (ch == ')') {
                openParenCount--;
                if (openParenCount < 0) {
                    return false;
                }
            }
        }

        return openParenCount == 0;
    }

    private static class FilterListener extends FiltersBaseListener {
        private final Filter result;

        public FilterListener(Filter result) {
            this.result = result;
        }

        @Override
        public void exitPlaystate(FiltersParser.PlaystateContext ctx) {
            if (ctx.ANY() != null) {
                result.setPlaystate(Playstate.ANY);
            } else if (ctx.PLAYED() != null) {
                result.setPlaystate(Playstate.PLAYED);
            } else if (ctx.UNPLAYED() != null) {
                result.setPlaystate(Playstate.UNPLAYED);
            }
        }

        @Override
        public void exitGenres(FiltersParser.GenresContext ctx) {
            if (ctx.listOfIntegers() != null) {
                for (TerminalNode node : ctx.listOfIntegers().INT()) {
                    int genre = Integer.parseInt(node.getText());
                    result.addGenre(genre);
                }
            }
        }

        @Override
        public void exitParentalRatings(FiltersParser.ParentalRatingsContext ctx) {
            if (ctx.listOfParentalRatings() != null) {
                for (TerminalNode node : ctx.listOfParentalRatings().RATING()) {
                    String parentRating = node.getText();
                    result.addParentRating(parentRating);
                }
            }
        }

        @Override
        public void exitStudios(FiltersParser.StudiosContext ctx) {
            if (ctx.listOfIntegers() != null) {
                for (TerminalNode node : ctx.listOfIntegers().INT()) {
                    int studio = Integer.parseInt(node.getText());
                    result.addStudio(studio);
                }
            }
        }

        @Override
        public void exitYears(FiltersParser.YearsContext ctx) {
            if (ctx.listOfIntegers() != null) {
                for (TerminalNode node : ctx.listOfIntegers().INT()) {
                    int year = Integer.parseInt(node.getText());
                    result.addYears(year);
                }
            }
        }

        @Override
        public void exitStatus(FiltersParser.StatusContext ctx) {
            if (ctx.ANY() != null) {
                result.setStatus(Status.ANY);
            } else if (ctx.CONTINUING() != null) {
                result.setStatus(Status.CONTINUING);
            } else if (ctx.ENDED() != null) {
                result.setStatus(Status.ENDED);
            }
        }
    }
}