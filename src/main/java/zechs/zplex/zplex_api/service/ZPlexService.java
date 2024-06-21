package zechs.zplex.zplex_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import zechs.zplex.zplex_api.model.Movie;
import zechs.zplex.zplex_api.model.tv.Episode;
import zechs.zplex.zplex_api.model.tv.Season;
import zechs.zplex.zplex_api.model.tv.Show;
import zechs.zplex.zplex_api.utils.EpisodeRowMapper;
import zechs.zplex.zplex_api.utils.MovieRowMapper;
import zechs.zplex.zplex_api.utils.SeasonRowMapper;
import zechs.zplex.zplex_api.utils.ShowRowMapper;

import java.util.List;

@Service
public class ZPlexService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ZPlexService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Movie> getAllMovies(String query) {
        String sql = "SELECT movie.* FROM movies movie WHERE movie.title ILIKE ? ORDER BY movie.id";
        String searchPattern = "%" + query + "%";
        return jdbcTemplate.query(sql, new MovieRowMapper(), searchPattern);
    }

    public List<Movie> getMovies(String query, int pageSize, int pageNumber) {
        if (pageSize <= 0 || pageNumber <= 0) {
            throw new IllegalArgumentException("Page size and page number must be greater than zero.");
        }
        int offset = (pageNumber - 1) * pageSize;
        String searchPattern = "%" + query + "%";
        Object[] args = {searchPattern, pageSize, offset};
        String sql = "SELECT movie.* FROM movies movie WHERE movie.title ILIKE ? ORDER BY movie.id LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new MovieRowMapper(), args);
    }

    public Movie getMovieById(int id) {
        String sql = "SELECT movie.* FROM movies movie WHERE movie.id = ?";
        return jdbcTemplate.queryForObject(sql, new MovieRowMapper(), id);
    }

    public int getMoviesPageCount(String query, int pageSize) {
        String sql = "SELECT COUNT(*) FROM movies WHERE title ILIKE ?";
        String searchPattern = "%" + query + "%";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, searchPattern);
        if (count == null || count == 0) {
            count = 1;
        }
        return (int) Math.ceil((double) count / pageSize);
    }

    public List<Show> getAllShows(String query) {
        String sql = "SELECT show.* FROM shows show WHERE show.name ILIKE ? ORDER BY show.id";
        String searchPattern = "%" + query + "%";
        return jdbcTemplate.query(sql, new ShowRowMapper(), searchPattern);
    }

    public List<Show> getShows(String query, int pageSize, int pageNumber) {
        if (pageSize <= 0 || pageNumber <= 0) {
            throw new IllegalArgumentException("Page size and page number must be greater than zero.");
        }
        int offset = (pageNumber - 1) * pageSize;
        String searchPattern = "%" + query + "%";
        Object[] args = {searchPattern, pageSize, offset};
        String sql = "SELECT show.* FROM shows show WHERE show.name ILIKE ? ORDER BY show.id LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new ShowRowMapper(), args);
    }

    public Show getShowById(int id) {
        String sql = "SELECT show.* FROM shows show WHERE show.id = ?";
        return jdbcTemplate.queryForObject(sql, new ShowRowMapper(), id);
    }

    public int getShowsPageCount(String query, int pageSize) {
        String sql = "SELECT COUNT(*) FROM shows WHERE name ILIKE ?";
        String searchPattern = "%" + query + "%";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, searchPattern);
        if (count == null || count == 0) {
            count = 1;
        }
        return (int) Math.ceil((double) count / pageSize);
    }

    public List<Season> getSeasons(Integer showId) {
        String sql = "SELECT season.* FROM seasons season WHERE season.show_id = ? ORDER BY season.season_number";
        return jdbcTemplate.query(sql, new SeasonRowMapper(), showId);
    }

    public List<Episode> getEpisodes(Integer seasonId) {
        String sql = "SELECT episode.* FROM episodes episode WHERE episode.season_id = ? ORDER BY episode.episode_number";
        return jdbcTemplate.query(sql, new EpisodeRowMapper(), seasonId);
    }

}
