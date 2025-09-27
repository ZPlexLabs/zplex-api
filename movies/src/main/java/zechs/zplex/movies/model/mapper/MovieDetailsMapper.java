package zechs.zplex.movies.model.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import zechs.zplex.config.model.IdNamePair;
import zechs.zplex.media.model.Cast;
import zechs.zplex.media.model.Crew;
import zechs.zplex.media.model.Studio;
import zechs.zplex.movies.model.MovieDetails;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MovieDetailsMapper implements RowMapper<MovieDetails> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public MovieDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            Integer tmdbId = rs.getInt("id");
            String title = rs.getString("title");
            Integer collectionId = rs.getObject("collection_id") != null ? rs.getInt("collection_id") : null;
            String fileId = rs.getString("file_id");
            String imdbId = rs.getString("imdb_id");
            String imdbRating = rs.getString("imdb_rating");
            Long imdbVotes = rs.getObject("imdb_votes") != null ? rs.getLong("imdb_votes") : null;
            String releaseDate = rs.getString("release_date");
            Short releaseYear = rs.getObject("release_year") != null ? rs.getShort("release_year") : null;
            String parentalRating = rs.getString("parental_rating");
            Short runtime = rs.getObject("runtime") != null ? rs.getShort("runtime") : null;
            String posterPath = rs.getString("poster_path");
            String backdropPath = rs.getString("backdrop_path");
            String logoPath = rs.getString("logo_image");
            String trailerLink = rs.getString("trailer_link");
            String tagline = rs.getString("tagline");
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
            String collectionsJson = rs.getString("collections");
            List<Map<String, Object>> rawCollections = List.of();

            if (collectionsJson != null && !collectionsJson.isEmpty()) {
                rawCollections = objectMapper.readValue(collectionsJson, new TypeReference<List<Map<String, Object>>>() {
                });
            }

            List<IdNamePair> collections = rawCollections.stream()
                    .map(map -> new IdNamePair((Integer) map.get("id"), (String) map.get("title")))
                    .toList();

            return new MovieDetails(tmdbId, title, collectionId, fileId, imdbId, imdbRating, imdbVotes, releaseDate, releaseYear, parentalRating, runtime, posterPath, backdropPath, logoPath, trailerLink, tagline, plot, director, genres, studios, casts, crews, collections);
        } catch (Exception e) {
            throw new SQLException("Error mapping MovieDetails", e);
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