package zechs.zplex.stream.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import zechs.zplex.auth.exception.UserDoesNotExist;
import zechs.zplex.auth.model.User;
import zechs.zplex.auth.service.UserAccessService;
import zechs.zplex.auth.utils.JwtUtil;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.common.model.UserAccess;
import zechs.zplex.config.service.ParentalRatingNormalizer;
import zechs.zplex.stream.model.StreamGrantResponse;

@Service
public class StreamGrantService {

    private final JdbcTemplate jdbcTemplate;
    private final UserAccessService userAccessService;
    private final ParentalRatingNormalizer ratingNormalizer;
    private final JwtUtil jwtUtil;

    public StreamGrantService(JdbcTemplate jdbcTemplate, UserAccessService userAccessService,
                              ParentalRatingNormalizer ratingNormalizer, JwtUtil jwtUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.userAccessService = userAccessService;
        this.ratingNormalizer = ratingNormalizer;
        this.jwtUtil = jwtUtil;
    }

    public StreamGrantResponse createGrant(User user, String fileId) throws UserDoesNotExist {
        UserAccess access = userAccessService.getAccess(user.getUsername());
        if (!user.getCapabilitiesAsEnum().contains(zechs.zplex.common.capability.Capabilities.STREAM)) {
            throw new StreamAccessDeniedException();
        }

        MediaFile mediaFile;
        try {
            mediaFile = jdbcTemplate.queryForObject("""
                    SELECT f.id, 1 AS library_id, m.id AS tmdb_id, m.parental_rating, 'MOVIE' AS media_type
                    FROM files f JOIN movies m ON m.file_id = f.id
                    WHERE f.id = ?
                    UNION ALL
                    SELECT f.id, 2 AS library_id, s.id AS tmdb_id, s.parental_rating, 'SHOW' AS media_type
                    FROM files f
                    JOIN episodes e ON e.file_id = f.id
                    JOIN seasons se ON se.id = e.season_id
                    JOIN shows s ON s.id = se.show_id
                    WHERE f.id = ?
                    LIMIT 1
                    """, (resultSet, rowNum) -> new MediaFile(
                            resultSet.getString("id"),
                            resultSet.getInt("library_id"),
                            resultSet.getInt("tmdb_id"),
                            resultSet.getString("parental_rating"),
                            MediaType.valueOf(resultSet.getString("media_type"))), fileId, fileId);
        } catch (EmptyResultDataAccessException exception) {
            throw new StreamAccessDeniedException();
        }

        Integer ratingRank = ratingNormalizer.rankOf(mediaFile.parentalRating());
        if (!access.isLibraryAllowed(mediaFile.libraryId())
                || !access.isRatingAllowed(ratingRank)
                || access.isBlacklisted(mediaFile.mediaType(), mediaFile.tmdbId())) {
            throw new StreamAccessDeniedException();
        }

        return new StreamGrantResponse(jwtUtil.generateStreamGrant(user, mediaFile.fileId()));
    }

    private record MediaFile(String fileId, int libraryId, int tmdbId, String parentalRating, MediaType mediaType) {
    }

    public static class StreamAccessDeniedException extends RuntimeException {
    }
}
