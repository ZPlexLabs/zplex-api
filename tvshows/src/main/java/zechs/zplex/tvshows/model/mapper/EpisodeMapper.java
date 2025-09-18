package zechs.zplex.tvshows.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.tvshows.model.Episode;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EpisodeMapper implements RowMapper<Episode> {
    @Override
    public Episode mapRow(ResultSet rs, int rowNum) throws SQLException {
        Integer episodeId = rs.getInt("id");
        String title = rs.getString("title");
        Short episodeNumber = rs.getObject("episode_number") != null ? rs.getShort("episode_number") : null;
        Short seasonNumber = rs.getObject("season_number") != null ? rs.getShort("season_number") : null;
        String overview = rs.getString("overview");
        Short runtime = rs.getObject("runtime") != null ? rs.getShort("runtime") : null;
        String releaseDate = rs.getString("release_date");
        String stillPath = rs.getString("still_path");
        String fileId = rs.getString("file_id");
        return new Episode(episodeId, title, episodeNumber, seasonNumber, overview, runtime, releaseDate, stillPath, fileId);
    }
}
