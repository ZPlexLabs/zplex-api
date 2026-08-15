package zechs.zplex.userdata.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zechs.zplex.auth.model.User;
import zechs.zplex.common.model.ErrorResponse;
import zechs.zplex.userdata.model.api.ContinueWatchingItem;
import zechs.zplex.userdata.model.api.PlayedRequest;
import zechs.zplex.userdata.model.api.PlayedResponse;
import zechs.zplex.userdata.model.api.ProgressUpdateRequest;
import zechs.zplex.userdata.model.api.WatchlistItemResponse;
import zechs.zplex.userdata.model.api.WatchlistRequest;
import zechs.zplex.userdata.service.PlayedService;
import zechs.zplex.userdata.service.WatchProgressService;
import zechs.zplex.userdata.service.WatchlistService;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private static final Logger LOGGER = Logger.getLogger(MeController.class.getName());

    private final WatchProgressService watchProgressService;
    private final WatchlistService watchlistService;
    private final PlayedService playedService;

    public MeController(WatchProgressService watchProgressService,
                       WatchlistService watchlistService,
                       PlayedService playedService) {
        this.watchProgressService = watchProgressService;
        this.watchlistService = watchlistService;
        this.playedService = playedService;
    }

    @PutMapping("/progress")
    @Operation(summary = "Upsert watch progress for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Progress saved"),
            @ApiResponse(responseCode = "400", description = "Invalid progress payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> updateProgress(@Valid @RequestBody ProgressUpdateRequest request,
                                            @AuthenticationPrincipal User user) {
        try {
            watchProgressService.upsertProgress(user.getUsername(), request);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while saving watch progress", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/continue-watching")
    @Operation(summary = "List in-progress titles for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "In-progress titles",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ContinueWatchingItem.class))
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> continueWatching(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(watchProgressService.getContinueWatching(user.getUsername()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while fetching continue-watching", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/watchlist")
    @Operation(summary = "List the authenticated user's watchlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Watchlist items",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = WatchlistItemResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> watchlist(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(watchlistService.list(user.getUsername()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while fetching watchlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/watchlist")
    @Operation(summary = "Add a title to the authenticated user's watchlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Added to watchlist"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> addToWatchlist(@Valid @RequestBody WatchlistRequest request,
                                            @AuthenticationPrincipal User user) {
        try {
            watchlistService.add(user.getUsername(), request.mediaType(), request.tmdbId());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while adding to watchlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/watchlist/{mediaType}/{tmdbId}")
    @Operation(summary = "Remove a title from the authenticated user's watchlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Removed from watchlist"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> removeFromWatchlist(@PathVariable zechs.zplex.common.model.MediaType mediaType,
                                                 @PathVariable Integer tmdbId,
                                                 @AuthenticationPrincipal User user) {
        try {
            watchlistService.remove(user.getUsername(), mediaType, tmdbId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while removing from watchlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/played")
    @Operation(summary = "List titles the authenticated user has marked as played")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Played items",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PlayedResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> played(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(playedService.list(user.getUsername()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while fetching played", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/played")
    @Operation(summary = "Mark a title as played for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Marked as played"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> markPlayed(@Valid @RequestBody PlayedRequest request,
                                        @AuthenticationPrincipal User user) {
        try {
            playedService.markPlayed(user.getUsername(), request.mediaType(), request.tmdbId(),
                    request.seasonNumberOrZero(), request.episodeNumberOrZero());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while marking played", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/played/{mediaType}/{tmdbId}")
    @Operation(summary = "Unmark a title as played for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Unmarked as played"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> unmarkPlayed(@PathVariable zechs.zplex.common.model.MediaType mediaType,
                                          @PathVariable Integer tmdbId,
                                          @RequestParam(defaultValue = "0") int seasonNumber,
                                          @RequestParam(defaultValue = "0") int episodeNumber,
                                          @AuthenticationPrincipal User user) {
        try {
            playedService.unmarkPlayed(user.getUsername(), mediaType, tmdbId, seasonNumber, episodeNumber);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while unmarking played", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}
