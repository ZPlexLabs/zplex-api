package zechs.zplex.zplex_api.utils;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.zplex_api.model.tv.Episode;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EpisodeRowMapper implements RowMapper<Episode> {
    @Override
    public Episode mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Episode(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getInt("episode_number"),
                rs.getString("still_path"),
                rs.getString("file_id")
        );
    }
}
