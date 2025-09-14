package zechs.zplex.movies.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.movies.model.LatestMovie;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LatestMovieMapper implements RowMapper<LatestMovie> {
    @Override
    public LatestMovie mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LatestMovie(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("poster_path"),
                rs.getString("backdrop_path"),
                rs.getString("release_year")
        );
    }
}