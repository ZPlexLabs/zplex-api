package zechs.zplex.suggestions.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import zechs.zplex.suggestions.model.SearchSuggestion;
import zechs.zplex.suggestions.model.Suggestion;
import zechs.zplex.suggestions.model.mapper.SearchSuggestionMapper;
import zechs.zplex.suggestions.model.mapper.SuggestionMapper;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class SuggestionsRepository {

    private static final Logger logger = Logger.getLogger(SuggestionsRepository.class.getName());

    private final JdbcTemplate jdbcTemplate;

    public SuggestionsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SearchSuggestion> getSearchSuggestions(int count) {
        String sql = "SELECT * FROM fetch_titles_for_today(?)";
        List<SearchSuggestion> result = jdbcTemplate.query(sql, new SearchSuggestionMapper(), count);
        logger.log(Level.INFO, "Fetched {0} search suggestions from the database", count);
        return result;
    }

    public List<Suggestion> getSuggestions(int count) {
        String sql = "SELECT * FROM fetch_titles_for_today(?)";
        List<Suggestion> result = jdbcTemplate.query(sql, new SuggestionMapper(), count);
        logger.log(Level.INFO, "Fetched {0} suggestions from the database", count);
        return result;
    }
}