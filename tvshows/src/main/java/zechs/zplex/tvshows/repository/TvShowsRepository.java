package zechs.zplex.tvshows.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.config.service.FilterConfigService;
import zechs.zplex.filter_parser.model.Filter;
import zechs.zplex.media.model.MediaListItem;
import zechs.zplex.media.model.query_filters.OrderBy;
import zechs.zplex.media.model.query_filters.SortBy;
import zechs.zplex.media.repository.MediaRepository;
import zechs.zplex.tvshows.model.LatestTvShow;
import zechs.zplex.tvshows.model.TvShowDetails;
import zechs.zplex.tvshows.model.mapper.LatestTvShowMapper;
import zechs.zplex.tvshows.model.mapper.TvShowDetailsMapper;

import java.util.List;
import java.util.logging.Logger;

@Repository
public class TvShowsRepository extends MediaRepository {

    private static final Logger LOGGER = Logger.getLogger(TvShowsRepository.class.getName());
    private static final String SHOWS_TABLE_NAME = "shows";
    private static final String SHOWS_DETAILS_VIEW_NAME = "show_details_mv";

    public TvShowsRepository(JdbcTemplate jdbcTemplate, FilterConfigService filterConfigService) {
        super(jdbcTemplate, filterConfigService, MediaType.SHOW);
    }

    @Override
    protected String getTableName() {
        return SHOWS_TABLE_NAME;
    }

    public List<LatestTvShow> getLatestShows(int count) {
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
                    GROUP BY s.id, s.title, s.poster_path, s.backdrop_path, s.release_year, s.release_year_to
                    ORDER BY MAX(f.modified_time) DESC
                    LIMIT ?
                """;
        LOGGER.info("Fetching " + count + " latest shows from database...");
        return jdbcTemplate.query(sql, new LatestTvShowMapper(), count);
    }

    public List<MediaListItem> getShows(Filter filter, SortBy sort, OrderBy order, Integer pageNumber, Integer pageSize, boolean includeNull) {
        return getMedia(filter, sort, order, pageNumber, pageSize, includeNull);
    }

    public Integer countShows(Filter filter, boolean includeNull) {
        return countMedia(filter, includeNull);
    }

    public TvShowDetails getShowById(Integer tmdbId) {
        String sql = "SELECT * from " + SHOWS_DETAILS_VIEW_NAME + " WHERE id = ? LIMIT 1";
        LOGGER.info("Fetching " + tmdbId + " TV Show details from database...");
        return jdbcTemplate.queryForObject(sql, new TvShowDetailsMapper(), tmdbId);
    }
}
