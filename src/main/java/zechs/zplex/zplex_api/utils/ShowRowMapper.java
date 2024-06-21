package zechs.zplex.zplex_api.utils;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.zplex_api.model.tv.Show;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowRowMapper implements RowMapper<Show> {
    @Override
    public Show mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Show(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("poster_path"),
                rs.getDouble("vote_average")
        );
    }
}