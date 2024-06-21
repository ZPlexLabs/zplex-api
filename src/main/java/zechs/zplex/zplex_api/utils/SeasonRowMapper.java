package zechs.zplex.zplex_api.utils;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.zplex_api.model.tv.Season;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SeasonRowMapper implements RowMapper<Season> {
    @Override
    public Season mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Season(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("poster_path"),
                rs.getInt("season_number")
        );
    }
}