package zechs.zplex.media.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.media.model.MediaListItem;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MediaListItemMapper implements RowMapper<MediaListItem> {
    @Override
    public MediaListItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        MediaListItem item = new MediaListItem();
        item.setTmdbId(rs.getInt("tmdbId"));
        item.setTitle(rs.getString("title"));
        item.setPosterPath(rs.getString("posterPath"));

        Object imdbRating = rs.getObject("imdbRating");
        if (imdbRating != null) {
            item.setImdbRating(Double.valueOf(imdbRating.toString()));
        } else {
            item.setImdbRating(null);
        }
        item.setRelease(rs.getString("release"));
        return item;
    }
}