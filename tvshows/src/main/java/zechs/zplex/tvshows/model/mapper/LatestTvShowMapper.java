package zechs.zplex.tvshows.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.tvshows.model.LatestTvShow;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LatestTvShowMapper implements RowMapper<LatestTvShow> {
    @Override
    public LatestTvShow mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LatestTvShow(
                rs.getInt("tmdbId"),
                rs.getString("title"),
                rs.getString("poster_path"),
                rs.getString("backdrop_path"),
                rs.getInt("episodes"),
                rs.getString("release")
        );
    }
}
