package zechs.zplex.tvshows.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.common.model.UserAccess;
import zechs.zplex.config.service.FilterConfigService;
import zechs.zplex.config.service.ParentalRatingNormalizer;
import zechs.zplex.filter_parser.model.Filter;
import zechs.zplex.media.model.MediaListItem;
import zechs.zplex.media.model.query_filters.OrderBy;
import zechs.zplex.media.model.query_filters.SortBy;
import zechs.zplex.media.repository.MediaRepository;
import zechs.zplex.tvshows.model.Episode;
import zechs.zplex.tvshows.model.LatestTvShow;
import zechs.zplex.tvshows.model.Season;
import zechs.zplex.tvshows.model.TvShowDetails;
import zechs.zplex.tvshows.model.mapper.EpisodeMapper;
import zechs.zplex.tvshows.model.mapper.LatestTvShowMapper;
import zechs.zplex.tvshows.model.mapper.SeasonMapper;
import zechs.zplex.tvshows.model.mapper.TvShowDetailsMapper;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class TvShowsRepository extends MediaRepository {

    private static final Logger LOGGER = Logger.getLogger(TvShowsRepository.class.getName());
    private static final String SHOWS_TABLE_NAME = "shows";
    private static final String SHOWS_DETAILS_VIEW_NAME = "show_details_mv";

    public TvShowsRepository(JdbcTemplate jdbcTemplate, FilterConfigService filterConfigService,
                             ParentalRatingNormalizer ratingNormalizer) {
        super(jdbcTemplate, filterConfigService, ratingNormalizer, MediaType.SHOW);
    }

    @Override
    protected String getTableName() {
        return SHOWS_TABLE_NAME;
    }

    public List<LatestTvShow> getLatestShows(int count, UserAccess access) {
        String sql = """
                    SELECT s.id AS tmdbId,
                           s.title,
                           s.poster_path,
                           s.backdrop_path,
                           COUNT(f.id) AS episodes,
                           CASE
                               WHEN s.release_year_to = 2147483647 THEN CONCAT(s.release_year, ' - Present')
                               WHEN s.release_year_to IS NULL THEN s.release_year::TEXT
                               ELSE s.release_year || ' - ' || s.release_year_to
                           END AS release
                    FROM shows s
                             INNER JOIN seasons se ON s.id = se.show_id
                             INNER JOIN episodes e ON se.id = e.season_id
                             INNER JOIN files f ON e.file_id = f.id
                    WHERE %s
                    GROUP BY s.id, s.title, s.poster_path, s.backdrop_path, s.release_year, s.release_year_to
                    ORDER BY MAX(f.modified_time) DESC
                    LIMIT ?
                """.formatted(accessPredicateSql("s"));
        LOGGER.info("Fetching " + count + " latest shows from database...");
        return jdbcTemplate.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            int index = bindAccessPredicate(ps, 1, connection, access);
            ps.setInt(index, count);
            return ps;
        }, new LatestTvShowMapper());
    }

    public List<MediaListItem> getShows(Filter filter, SortBy sort, OrderBy order, Integer pageNumber, Integer pageSize, boolean includeNull, UserAccess access) {
        return getMedia(filter, sort, order, pageNumber, pageSize, includeNull, access);
    }

    public Integer countShows(Filter filter, boolean includeNull, UserAccess access) {
        return countMedia(filter, includeNull, access);
    }

    public TvShowDetails getShowById(Integer tmdbId) {
        String sql = "SELECT * from " + SHOWS_DETAILS_VIEW_NAME + " WHERE id = ? LIMIT 1";
        LOGGER.info("Fetching " + tmdbId + " TV Show details from database...");
        return jdbcTemplate.queryForObject(sql, new TvShowDetailsMapper(), tmdbId);
    }

    // Lightweight rating lookup for access checks on child endpoints; throws EmptyResultDataAccessException if the show is missing.
    public String getShowRating(Integer tmdbId) {
        String sql = "SELECT parental_rating FROM shows WHERE id = ? LIMIT 1";
        return jdbcTemplate.queryForObject(sql, String.class, tmdbId);
    }


    public List<Season> getSeasonsByShowId(Integer tmdbId) {
        String sql = """
                WITH episode_count AS (SELECT season_id, COUNT(*) AS episode_count FROM episodes GROUP BY season_id)
                SELECT s.id,
                       s.name,
                       to_char(to_timestamp(s.release_date / 1000), 'DD/MM/YYYY') AS release_date,
                       s.release_year,
                       s.poster_path,
                       s.season_number,
                       ec.episode_count
                FROM seasons s
                         LEFT JOIN episode_count ec ON s.id = ec.season_id
                WHERE s.show_id = ?
                ORDER BY s.season_number
                """;
        LOGGER.info("Fetching seasons for TV Show ID " + tmdbId + " from database...");
        return jdbcTemplate.query(sql, new SeasonMapper(), tmdbId);
    }

    public List<Episode> getEpisodesBySeasonId(Integer seasonId) {
        String sql = """
                SELECT id, title, episode_number, season_number, overview, runtime,
                       To_char(To_timestamp(airdate / 1000), 'DD/MM/YYYY') AS release_date,
                       still_path, file_id
                FROM   episodes
                WHERE  season_id = ?
                ORDER  BY episode_number
                """;
        LOGGER.info("Fetching episodes for Season ID " + seasonId + " from database...");
        return jdbcTemplate.query(sql, new EpisodeMapper(), seasonId);
    }
}
