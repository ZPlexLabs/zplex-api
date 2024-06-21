package zechs.zplex.zplex_api.utils;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.zplex_api.model.Movie;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieRowMapper implements RowMapper<Movie> {
    @Override
    public Movie mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Movie(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("poster_path"),
                rs.getDouble("vote_average"),
                rs.getInt("year"),
                rs.getString("file_id")
        );
    }
}
