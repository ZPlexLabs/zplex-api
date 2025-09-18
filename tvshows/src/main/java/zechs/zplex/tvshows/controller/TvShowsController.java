package zechs.zplex.tvshows.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zechs.zplex.common.model.ErrorResponse;
import zechs.zplex.common.model.PaginatedResponse;
import zechs.zplex.filter_parser.FilterExtractor;
import zechs.zplex.filter_parser.model.Filter;
import zechs.zplex.media.model.MediaListItem;
import zechs.zplex.media.model.query_filters.OrderBy;
import zechs.zplex.media.model.query_filters.SortBy;
import zechs.zplex.tvshows.model.LatestTvShow;
import zechs.zplex.tvshows.model.Season;
import zechs.zplex.tvshows.model.TvShowDetails;
import zechs.zplex.tvshows.repository.TvShowsRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/tvshows")
public class TvShowsController {

    private static final Logger LOGGER = Logger.getLogger(TvShowsController.class.getName());

    private static final int LATEST_SHOWS_COUNT = 10;
    private final TvShowsRepository tvShowsRepository;

    @Autowired
    public TvShowsController(TvShowsRepository tvShowsRepository) {
        this.tvShowsRepository = tvShowsRepository;
    }

    @GetMapping("/latest")
    @Operation(summary = "Get recently added shows")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully fetched latest 10 shows.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = LatestTvShow.class))
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> getLatestShows() {
        try {
            List<LatestTvShow> latestTvShows = tvShowsRepository.getLatestShows(LATEST_SHOWS_COUNT);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(latestTvShows);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while fetching latest shows", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("")
    @Operation(summary = "Get paginated shows with filter support.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully fetched list of shows.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = MediaListItem.class))
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> getShows(
            @RequestParam(required = false) Optional<SortBy> sortBy,
            @RequestParam(required = false) Optional<OrderBy> orderBy,
            @RequestParam(required = false) Optional<String> filterBy,
            @RequestParam(required = false) Optional<Integer> pageNumber,
            @RequestParam(required = false) Optional<Integer> pageSize,
            @RequestParam(required = false, defaultValue = "true") Boolean includeNull
    ) {
        try {
            SortBy sortByValue = sortBy.orElse(SortBy.TITLE);
            OrderBy orderByValue = orderBy.orElse(OrderBy.ASC);
            String filterByValue = filterBy.orElse("");
            Integer pageNumberByValue = pageNumber.orElse(1);
            Integer pageSizeByValue = pageSize.orElse(25);

            Filter filters = FilterExtractor.parseFilters(filterByValue);
            List<MediaListItem> shows = tvShowsRepository.getShows(filters, sortByValue, orderByValue, pageNumberByValue, pageSizeByValue, includeNull);
            Integer count = tvShowsRepository.countShows(filters, includeNull);

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PaginatedResponse<>(shows, pageNumberByValue, count));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while fetching shows", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{tmdbId}")
    @Operation(summary = "Get detailed information of a TV Show by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully fetched TV Show details.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TvShowDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "TV Show not found.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> getShowById(@PathVariable("tmdbId") Integer tmdbId) {
        try {
            TvShowDetails show = tvShowsRepository.getShowById(tmdbId);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(show);
        } catch (EmptyResultDataAccessException e) {
            LOGGER.log(Level.WARNING, "No TV Show found with id " + tmdbId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("TV Show with id " + tmdbId + " does not exists"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while fetching TV Show details for id=" + tmdbId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{tmdbId}/seasons")
    @Operation(summary = "Get all seasons of a TV Show by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully fetched seasons of the TV Show.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Season.class))
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "TV Show or seasons not found.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> getSeasonsByShowId(@PathVariable("tmdbId") Integer tmdbId) {
        try {
            List<Season> seasons = tvShowsRepository.getSeasonsByShowId(tmdbId);
            if (seasons == null || seasons.isEmpty()) {
                LOGGER.log(Level.WARNING, "No seasons found for TV Show with id " + tmdbId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ErrorResponse("No seasons found for TV Show with id " + tmdbId));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(seasons);
        } catch (EmptyResultDataAccessException e) {
            LOGGER.log(Level.WARNING, "No TV Show found with id " + tmdbId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("TV Show with id " + tmdbId + " does not exist"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while fetching seasons for TV Show id=" + tmdbId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

}
