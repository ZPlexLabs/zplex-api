package zechs.zplex.suggestions.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;
import zechs.zplex.common.model.Library;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.common.model.UserAccess;
import zechs.zplex.config.service.FilterConfigService;
import zechs.zplex.config.service.ParentalRatingNormalizer;
import zechs.zplex.suggestions.model.SearchSuggestion;
import zechs.zplex.suggestions.model.Suggestion;
import zechs.zplex.suggestions.model.mapper.SearchSuggestionMapper;
import zechs.zplex.suggestions.model.mapper.SuggestionMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class SuggestionsRepository {

    private static final Logger logger = Logger.getLogger(SuggestionsRepository.class.getName());
    private static final String SQL = "SELECT * FROM fetch_titles_for_today(?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final FilterConfigService filterConfigService;
    private final ParentalRatingNormalizer ratingNormalizer;

    public SuggestionsRepository(JdbcTemplate jdbcTemplate, FilterConfigService filterConfigService,
                                 ParentalRatingNormalizer ratingNormalizer) {
        this.jdbcTemplate = jdbcTemplate;
        this.filterConfigService = filterConfigService;
        this.ratingNormalizer = ratingNormalizer;
    }

    public List<SearchSuggestion> getSearchSuggestions(int count, UserAccess access) {
        List<SearchSuggestion> result = jdbcTemplate.query(statement(count, access), new SearchSuggestionMapper());
        logger.log(Level.INFO, "Fetched {0} search suggestions from the database", result.size());
        return result;
    }

    public List<Suggestion> getSuggestions(int count, UserAccess access) {
        List<Suggestion> result = jdbcTemplate.query(statement(count, access), new SuggestionMapper());
        logger.log(Level.INFO, "Fetched {0} suggestions from the database", result.size());
        return result;
    }

    private PreparedStatementCreator statement(int count, UserAccess access) {
        return connection -> {
            List<String> distinctRatings = new ArrayList<>();
            distinctRatings.addAll(filterConfigService.getFilterConfig(MediaType.MOVIE).getParentalRatings());
            distinctRatings.addAll(filterConfigService.getFilterConfig(MediaType.SHOW).getParentalRatings());

            PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setInt(1, count);
            ps.setBoolean(2, access.isLibraryAllowed(Library.MOVIES.getId()));
            ps.setBoolean(3, access.isLibraryAllowed(Library.SHOWS.getId()));
            setArrayExact(ps, 4, connection, ratingNormalizer.allowedRatings(distinctRatings, access.getMaxRatingRank()), "TEXT");
            ps.setBoolean(5, access.isAllowUnrated());
            setArrayExact(ps, 6, connection, ratingNormalizer.unratedRatings(distinctRatings), "TEXT");
            setArrayExact(ps, 7, connection, access.getBlacklistedTmdbIds(MediaType.MOVIE).toArray(new Integer[0]), "INTEGER");
            setArrayExact(ps, 8, connection, access.getBlacklistedTmdbIds(MediaType.SHOW).toArray(new Integer[0]), "INTEGER");
            return ps;
        };
    }

    // Sends an empty array as a real empty array (a restriction), reserving SQL NULL for "no restriction".
    private void setArrayExact(PreparedStatement ps, int index, Connection connection, Object[] values, String sqlType) throws SQLException {
        if (values == null) {
            ps.setNull(index, Types.ARRAY);
        } else {
            ps.setArray(index, connection.createArrayOf(sqlType, values));
        }
    }
}