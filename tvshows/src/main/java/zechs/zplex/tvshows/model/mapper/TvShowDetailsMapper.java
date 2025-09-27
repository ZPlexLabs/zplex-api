package zechs.zplex.tvshows.model.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.config.model.IdNamePair;
import zechs.zplex.media.model.Cast;
import zechs.zplex.media.model.Crew;
import zechs.zplex.media.model.Studio;
import zechs.zplex.tvshows.model.Season;
import zechs.zplex.tvshows.model.TvShowDetails;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TvShowDetailsMapper implements RowMapper<TvShowDetails> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TvShowDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            Integer tmdbId = rs.getInt("id");
            String title = rs.getString("title");
            String imdbId = rs.getString("imdb_id");
            String imdbRating = rs.getString("imdb_rating");
            Long imdbVotes = rs.getObject("imdb_votes") != null ? rs.getLong("imdb_votes") : null;
            String releaseDate = rs.getString("release_date");
            String release = rs.getString("release");
            String parentalRating = rs.getString("parental_rating");
            String posterPath = rs.getString("poster_path");
            String backdropPath = rs.getString("backdrop_path");
            String logoPath = rs.getString("logo_image");
            String trailerLink = rs.getString("trailer_link");
            String plot = rs.getString("plot");
            String director = rs.getString("director");

            List<IdNamePair> genres = parseJsonList(rs.getString("genres"), new TypeReference<List<IdNamePair>>() {
            });
            List<Studio> studios = parseJsonList(rs.getString("studios"), new TypeReference<List<Studio>>() {
            });
            List<Cast> casts = parseJsonList(rs.getString("cast"), new TypeReference<List<Cast>>() {
            });
            List<Crew> crews = parseJsonList(rs.getString("crew"), new TypeReference<List<Crew>>() {
            });

            String latestSeasonJson = rs.getString("latest_season");
            Season latestSeason = null;
            if (latestSeasonJson != null && !latestSeasonJson.isEmpty()) {
                latestSeason = objectMapper.readValue(latestSeasonJson, Season.class);
            }
            return new TvShowDetails(tmdbId, title, imdbId, imdbRating, imdbVotes, releaseDate, release, parentalRating, posterPath, backdropPath, logoPath, trailerLink, plot, director, genres, studios, casts, crews, latestSeason);
        } catch (Exception e) {
            throw new SQLException("Error mapping TvShowDetails", e);
        }
    }

    private <T> List<T> parseJsonList(String json, TypeReference<List<T>> typeReference) {
        try {
            if (json == null || json.isEmpty()) {
                return List.of();
            }
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON: " + json, e);
        }
    }
}