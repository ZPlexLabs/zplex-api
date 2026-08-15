package zechs.zplex.media.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.common.model.UserAccess;
import zechs.zplex.config.model.FilterConfig;
import zechs.zplex.config.model.RatingRank;
import zechs.zplex.config.service.FilterConfigService;
import zechs.zplex.config.service.ParentalRatingNormalizer;
import zechs.zplex.filter_parser.model.Filter;
import zechs.zplex.filter_parser.utils.FilterSanitizer;
import zechs.zplex.filter_parser.utils.FilterValidator;
import zechs.zplex.media.model.MediaListItem;
import zechs.zplex.media.model.mapper.MediaListItemMapper;
import zechs.zplex.media.model.query_filters.OrderBy;
import zechs.zplex.media.model.query_filters.SortBy;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MediaRepository {

    private static final Logger LOGGER = Logger.getLogger(MediaRepository.class.getName());
    private static final int MAX_DEFINED_RANK =
            Arrays.stream(RatingRank.values()).mapToInt(RatingRank::getRank).max().orElse(Integer.MAX_VALUE);

    protected final JdbcTemplate jdbcTemplate;
    protected final FilterConfig filterConfig;
    protected final MediaType mediaType;
    protected final ParentalRatingNormalizer ratingNormalizer;

    protected MediaRepository(JdbcTemplate jdbcTemplate, FilterConfigService filterConfigService,
                              ParentalRatingNormalizer ratingNormalizer, MediaType mediaType) {
        this.jdbcTemplate = jdbcTemplate;
        this.mediaType = mediaType;
        this.ratingNormalizer = ratingNormalizer;
        this.filterConfig = filterConfigService.getFilterConfig(mediaType);
    }

    protected abstract String getTableName();

    public List<MediaListItem> getMedia(Filter filter, SortBy sort, OrderBy order,
                                        Integer pageNumber, Integer pageSize, boolean includeNull, UserAccess access) {
        FilterValidator.validateFilters(filter, filterConfig);
        FilterSanitizer.removeDuplicates(filter);

        int validatedPageNumber = pageNumber == null || pageNumber < 0 ? 0 : pageNumber;
        int validatedPageSize = pageSize == null || pageSize <= 0 ? 25 : pageSize;

        LOGGER.log(Level.INFO, "Fetching media: filter={0}, sort={1}, order={2}, pageNumber={3}, pageSize={4}, includeNull={5}",
                new Object[]{filter, sort, order, validatedPageNumber, validatedPageSize, includeNull});

        String sql = "SELECT * FROM search_media(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.query(connection ->
                        prepareMediaStatement(connection, sql, filter, sort, order, includeNull, validatedPageNumber, validatedPageSize, access),
                new MediaListItemMapper());
    }

    public Integer countMedia(Filter filter, boolean includeNull, UserAccess access) {
        FilterValidator.validateFilters(filter, filterConfig);
        FilterSanitizer.removeDuplicates(filter);

        LOGGER.log(Level.INFO, "Counting media: filter={0}, includeNull={1}", new Object[]{filter, includeNull});

        String sql = "SELECT count_media(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) AS total";

        List<Integer> results = jdbcTemplate.query(connection ->
                        prepareMediaStatement(connection, sql, filter, null, null, includeNull, null, null, access),
                (rs, rowNum) -> rs.getInt("total"));

        return results.isEmpty() ? 0 : results.getFirst();
    }

    private PreparedStatement prepareMediaStatement(Connection connection, String sql, Filter filter,
                                                    SortBy sort, OrderBy order, boolean includeNull,
                                                    Integer pageNumber, Integer pageSize, UserAccess access) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        int paramIndex = 1;

        ps.setString(paramIndex++, getTableName());
        setArrayOrNull(ps, paramIndex++, connection, filter.getStudios().toArray(new Integer[0]), "INTEGER");
        setArrayOrNull(ps, paramIndex++, connection, filter.getParentalRatings().toArray(new String[0]), "TEXT");
        setArrayOrNull(ps, paramIndex++, connection, filter.getYears().toArray(new Integer[0]), "INTEGER");
        setArrayOrNull(ps, paramIndex++, connection, filter.getGenres().toArray(new Integer[0]), "INTEGER");

        if (sort != null && order != null) {
            ps.setString(paramIndex++, sort.name().toLowerCase(Locale.ENGLISH));
            ps.setString(paramIndex++, order.name());
        }

        ps.setBoolean(paramIndex++, includeNull);

        if (pageNumber != null && pageSize != null) {
            ps.setInt(paramIndex++, pageNumber);
            ps.setInt(paramIndex++, pageSize);
        }

        paramIndex = setAccessParams(ps, paramIndex, connection, access);

        return ps;
    }

    // Binds the four trailing access params: allowed ratings (NULL = no ceiling), allowUnrated, unrated ratings, blacklist ids.
    private int setAccessParams(PreparedStatement ps, int index, Connection connection, UserAccess access) throws SQLException {
        setArrayExact(ps, index++, connection, allowedRatings(access), "TEXT");
        ps.setBoolean(index++, access.isAllowUnrated());
        setArrayExact(ps, index++, connection, unratedRatings(), "TEXT");
        Set<Integer> blacklist = access.getBlacklistedTmdbIds(mediaType);
        setArrayExact(ps, index++, connection, blacklist.toArray(new Integer[0]), "INTEGER");
        return index;
    }

    // Access WHERE fragment for inline queries (7 placeholders): keeps rows within the rating ceiling (or unrated when allowed) and not blacklisted.
    protected String accessPredicateSql(String alias) {
        return "(?::text[] IS NULL"
                + " OR " + alias + ".parental_rating = ANY(?::text[])"
                + " OR (? AND (" + alias + ".parental_rating IS NULL"
                + " OR (?::text[] IS NOT NULL AND " + alias + ".parental_rating = ANY(?::text[])))))"
                + " AND (?::int[] IS NULL OR " + alias + ".id <> ALL(?::int[]))";
    }

    // Binds the 7 placeholders of accessPredicateSql in order; returns the next index.
    protected int bindAccessPredicate(PreparedStatement ps, int index, Connection connection, UserAccess access) throws SQLException {
        String[] allowed = allowedRatings(access);
        String[] unrated = unratedRatings();
        Integer[] blacklist = access.getBlacklistedTmdbIds(mediaType).toArray(new Integer[0]);
        setArrayExact(ps, index++, connection, allowed, "TEXT");
        setArrayExact(ps, index++, connection, allowed, "TEXT");
        ps.setBoolean(index++, access.isAllowUnrated());
        setArrayExact(ps, index++, connection, unrated, "TEXT");
        setArrayExact(ps, index++, connection, unrated, "TEXT");
        setArrayExact(ps, index++, connection, blacklist, "INTEGER");
        setArrayExact(ps, index++, connection, blacklist, "INTEGER");
        return index;
    }

    private String[] allowedRatings(UserAccess access) {
        if (access.getMaxRatingRank() >= MAX_DEFINED_RANK) {
            return null; // no ceiling
        }
        return filterConfig.getParentalRatings().stream()
                .filter(rating -> {
                    Integer rank = ratingNormalizer.rankOf(rating);
                    return rank != null && rank <= access.getMaxRatingRank();
                })
                .toArray(String[]::new);
    }

    private String[] unratedRatings() {
        return filterConfig.getParentalRatings().stream()
                .filter(rating -> ratingNormalizer.rankOf(rating) == null)
                .toArray(String[]::new);
    }

    private Array createArrayOrNull(Connection connection, Object[] values, String sqlType) throws SQLException {
        if (values == null || values.length == 0) {
            return null;
        }
        return connection.createArrayOf(sqlType, values);
    }

    private void setArrayOrNull(PreparedStatement ps, int index, Connection connection, Object[] values, String sqlType) throws SQLException {
        Array array = createArrayOrNull(connection, values, sqlType);
        if (array == null) {
            ps.setNull(index, java.sql.Types.ARRAY);
        } else {
            ps.setArray(index, array);
        }
    }

    // Sends an empty array as a real empty array (a restriction), reserving SQL NULL for "no restriction".
    private void setArrayExact(PreparedStatement ps, int index, Connection connection, Object[] values, String sqlType) throws SQLException {
        if (values == null) {
            ps.setNull(index, java.sql.Types.ARRAY);
        } else {
            ps.setArray(index, connection.createArrayOf(sqlType, values));
        }
    }
}