package zechs.zplex.media.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.config.model.FilterConfig;
import zechs.zplex.config.service.FilterConfigService;
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
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MediaRepository {

    private static final Logger LOGGER = Logger.getLogger(MediaRepository.class.getName());
    protected final JdbcTemplate jdbcTemplate;
    protected final FilterConfig filterConfig;
    protected final MediaType mediaType;

    protected MediaRepository(JdbcTemplate jdbcTemplate, FilterConfigService filterConfigService, MediaType mediaType) {
        this.jdbcTemplate = jdbcTemplate;
        this.mediaType = mediaType;
        this.filterConfig = filterConfigService.getFilterConfig(mediaType);
    }

    protected abstract String getTableName();

    public List<MediaListItem> getMedia(Filter filter, SortBy sort, OrderBy order,
                                        Integer pageNumber, Integer pageSize, boolean includeNull) {
        FilterValidator.validateFilters(filter, filterConfig);
        FilterSanitizer.removeDuplicates(filter);

        int validatedPageNumber = pageNumber == null || pageNumber < 0 ? 0 : pageNumber;
        int validatedPageSize = pageSize == null || pageSize <= 0 ? 25 : pageSize;

        LOGGER.log(Level.INFO, "Fetching media: filter={0}, sort={1}, order={2}, pageNumber={3}, pageSize={4}, includeNull={5}",
                new Object[]{filter, sort, order, validatedPageNumber, validatedPageSize, includeNull});

        String sql = "SELECT * FROM search_media(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.query(connection ->
                        prepareMediaStatement(connection, sql, filter, sort, order, includeNull, validatedPageNumber, validatedPageSize),
                new MediaListItemMapper());
    }

    public Integer countMedia(Filter filter, boolean includeNull) {
        FilterValidator.validateFilters(filter, filterConfig);
        FilterSanitizer.removeDuplicates(filter);

        LOGGER.log(Level.INFO, "Counting media: filter={0}, includeNull={1}", new Object[]{filter, includeNull});

        String sql = "SELECT count_media(?, ?, ?, ?, ?, ?) AS total";

        List<Integer> results = jdbcTemplate.query(connection ->
                        prepareMediaStatement(connection, sql, filter, null, null, includeNull, null, null),
                (rs, rowNum) -> rs.getInt("total"));

        return results.isEmpty() ? 0 : results.getFirst();
    }

    private PreparedStatement prepareMediaStatement(Connection connection, String sql, Filter filter,
                                                    SortBy sort, OrderBy order, boolean includeNull,
                                                    Integer pageNumber, Integer pageSize) throws SQLException {
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

        return ps;
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
}