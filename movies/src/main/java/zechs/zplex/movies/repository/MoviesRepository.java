package zechs.zplex.movies.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.config.service.FilterConfigService;
import zechs.zplex.filter_parser.model.Filter;
import zechs.zplex.media.model.MediaListItem;
import zechs.zplex.media.model.query_filters.OrderBy;
import zechs.zplex.media.model.query_filters.SortBy;
import zechs.zplex.media.repository.MediaRepository;
import zechs.zplex.movies.model.LatestMovie;
import zechs.zplex.movies.model.MovieDetails;
import zechs.zplex.movies.model.mapper.LatestMovieMapper;
import zechs.zplex.movies.model.mapper.MovieDetailsMapper;

import java.util.List;
import java.util.logging.Logger;

@Repository
public class MoviesRepository extends MediaRepository {

    private static final Logger LOGGER = Logger.getLogger(MoviesRepository.class.getName());
    private static final String MOVIES_TABLE_NAME = "movies";
    private static final String MOVIES_DETAILS_VIEW_NAME = "movie_details_mv";

    public MoviesRepository(JdbcTemplate jdbcTemplate, FilterConfigService filterConfigService) {
        super(jdbcTemplate, filterConfigService, MediaType.MOVIE);
    }

    @Override
    protected String getTableName() {
        return MOVIES_TABLE_NAME;
    }

    public List<LatestMovie> getLatestMovies(int count) {
        String sql = "SELECT m.id, m.title, m.poster_path, m.backdrop_path, m.release_year " +
                "FROM movies m INNER JOIN files f ON m.file_id = f.id " +
                "ORDER BY f.modified_time DESC LIMIT ?";
        LOGGER.info("Fetching " + count + " latest movies from database...");
        return jdbcTemplate.query(sql, new LatestMovieMapper(), count);
    }

    public List<MediaListItem> getMovies(Filter filter, SortBy sort, OrderBy order, Integer pageNumber, Integer pageSize, boolean includeNull) {
        return getMedia(filter, sort, order, pageNumber, pageSize, includeNull);
    }

    public Integer countMovies(Filter filter, boolean includeNull) {
        return countMedia(filter, includeNull);
    }

    public MovieDetails getMovieById(Integer tmdbId) {
        String sql = "SELECT * from " + MOVIES_DETAILS_VIEW_NAME + " WHERE id = ? LIMIT 1";
        LOGGER.info("Fetching " + tmdbId + " movie details from database...");
        return jdbcTemplate.queryForObject(sql, new MovieDetailsMapper(), tmdbId);
    }

}
