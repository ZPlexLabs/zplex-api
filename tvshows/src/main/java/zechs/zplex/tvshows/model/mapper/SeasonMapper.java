package zechs.zplex.tvshows.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.tvshows.model.Season;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SeasonMapper implements RowMapper<Season> {
    @Override
    public Season mapRow(ResultSet rs, int rowNum) throws SQLException {
        Integer seasonId = rs.getInt("id");
        String seasonName = rs.getString("name");
        String releaseDate = rs.getString("release_date");
        Short releaseYear = rs.getObject("release_year") != null ? rs.getShort("release_year") : null;
        String posterPath = rs.getString("poster_path");
        Short seasonNumber = rs.getObject("season_number") != null ? rs.getShort("season_number") : null;
        Short episodeCount = rs.getObject("episode_count") != null ? rs.getShort("episode_count") : null;
        return new Season(seasonId, seasonName, posterPath, releaseDate, releaseYear, seasonNumber, episodeCount);
    }
}
