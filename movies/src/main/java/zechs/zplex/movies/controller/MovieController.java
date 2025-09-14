package zechs.zplex.movies.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
import zechs.zplex.movies.model.LatestMovie;
import zechs.zplex.movies.repository.MoviesRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/movie")
public class MovieController {

    private static final Logger LOGGER = Logger.getLogger(MovieController.class.getName());

    private static final int LATEST_MOVIES_COUNT = 10;
    private final MoviesRepository moviesRepository;

    @Autowired
    public MovieController(MoviesRepository moviesRepository) {
        this.moviesRepository = moviesRepository;
    }

    @GetMapping("/latest")
    @Operation(summary = "Get recently added movies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully fetched latest 10 movies.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = LatestMovie.class))
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
    public ResponseEntity<?> getLatestMovies() {
        try {
            List<LatestMovie> latestTvShows = moviesRepository.getLatestMovies(LATEST_MOVIES_COUNT);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(latestTvShows);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while fetching latest movies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("")
    @Operation(summary = "Get paginated movies with filter support.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully fetched list of movies.",
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
    public ResponseEntity<?> getMovies(
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
            List<MediaListItem> movies = moviesRepository.getMovies(filters, sortByValue, orderByValue, pageNumberByValue, pageSizeByValue, includeNull);
            Integer count = moviesRepository.countMovies(filters, includeNull);

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PaginatedResponse<>(movies, pageNumberByValue, count));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while fetching movies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

}
