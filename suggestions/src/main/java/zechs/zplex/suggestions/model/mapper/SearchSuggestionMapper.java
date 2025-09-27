package zechs.zplex.suggestions.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.suggestions.model.SearchSuggestion;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchSuggestionMapper implements RowMapper<SearchSuggestion> {
    @Override
    public SearchSuggestion mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new SearchSuggestion(
                rs.getInt("tmdbid"),
                rs.getString("title"),
                rs.getString("type")
        );
    }
}