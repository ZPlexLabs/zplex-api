package zechs.zplex.suggestions.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.suggestions.model.Suggestion;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SuggestionMapper implements RowMapper<Suggestion> {
    @Override
    public Suggestion mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Suggestion(
                rs.getInt("tmdbid"),
                rs.getString("title"),
                rs.getObject("poster_path", String.class),
                rs.getObject("backdrop_path", String.class),
                rs.getObject("release", String.class),
                rs.getString("type")
        );
    }
}