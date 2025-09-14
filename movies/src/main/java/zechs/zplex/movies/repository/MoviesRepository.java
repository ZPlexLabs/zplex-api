package zechs.zplex.movies.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.config.model.FilterConfig;
import zechs.zplex.config.service.FilterConfigService;
import zechs.zplex.movies.model.LatestMovie;
import zechs.zplex.movies.model.mapper.LatestMovieMapper;

import java.util.List;
import java.util.logging.Logger;

@Repository
public class MoviesRepository {

    private static final Logger LOGGER = Logger.getLogger(MoviesRepository.class.getName());

    private final JdbcTemplate jdbcTemplate;
    private final FilterConfig filterConfig;

    public MoviesRepository(JdbcTemplate jdbcTemplate, FilterConfigService filterConfigService) {
        this.jdbcTemplate = jdbcTemplate;
        this.filterConfig = filterConfigService.getFilterConfig(MediaType.MOVIE);
    }

    public List<LatestMovie> getLatestMovies(int count) {
        String sql = "SELECT m.id, m.title, m.poster_path, m.backdrop_path, m.release_year " +
                "FROM movies m INNER JOIN files f ON m.file_id = f.id " +
                "ORDER BY f.modified_time DESC LIMIT ?";
        LOGGER.info("Fetching " + count + " latest movies from database...");
        return jdbcTemplate.query(sql, new LatestMovieMapper(), count);
    }

}
