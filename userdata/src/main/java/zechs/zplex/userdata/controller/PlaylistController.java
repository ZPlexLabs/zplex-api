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
import org.springframework.web.bind.annotation.RestController;
import zechs.zplex.auth.model.User;
import zechs.zplex.common.model.ErrorResponse;
import zechs.zplex.userdata.model.api.PlaylistDetailResponse;
import zechs.zplex.userdata.model.api.PlaylistItemRequest;
import zechs.zplex.userdata.model.api.PlaylistNameRequest;
import zechs.zplex.userdata.model.api.PlaylistReorderRequest;
import zechs.zplex.userdata.model.api.PlaylistResponse;
import zechs.zplex.userdata.service.PlaylistService;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/me/playlists")
public class PlaylistController {

    private static final Logger LOGGER = Logger.getLogger(PlaylistController.class.getName());

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    @Operation(summary = "List the authenticated user's playlists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Playlists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PlaylistResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> list(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(playlistService.list(user.getUsername()));
        } catch (Exception e) {
            return serverError("Exception while listing playlists", e);
        }
    }

    @PostMapping
    @Operation(summary = "Create a playlist for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlaylistResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> create(@Valid @RequestBody PlaylistNameRequest request,
                                    @AuthenticationPrincipal User user) {
        try {
            PlaylistResponse created = playlistService.create(user.getUsername(), request.name());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return serverError("Exception while creating playlist", e);
        }
    }

    @GetMapping("/{playlistId}")
    @Operation(summary = "Get a playlist with its ordered items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Playlist",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlaylistDetailResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "No matching playlist for this user"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> get(@PathVariable Long playlistId,
                                 @AuthenticationPrincipal User user) {
        try {
            return playlistService.get(user.getUsername(), playlistId)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            return serverError("Exception while fetching playlist", e);
        }
    }

    @PutMapping("/{playlistId}")
    @Operation(summary = "Rename a playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Renamed"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "404", description = "No matching playlist for this user"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> rename(@PathVariable Long playlistId,
                                    @Valid @RequestBody PlaylistNameRequest request,
                                    @AuthenticationPrincipal User user) {
        try {
            return toStatus(playlistService.rename(user.getUsername(), playlistId, request.name()));
        } catch (Exception e) {
            return serverError("Exception while renaming playlist", e);
        }
    }

    @DeleteMapping("/{playlistId}")
    @Operation(summary = "Delete a playlist and its items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "No matching playlist for this user"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> delete(@PathVariable Long playlistId,
                                    @AuthenticationPrincipal User user) {
        try {
            return toStatus(playlistService.delete(user.getUsername(), playlistId));
        } catch (Exception e) {
            return serverError("Exception while deleting playlist", e);
        }
    }

    @PostMapping("/{playlistId}/items")
    @Operation(summary = "Append a title to a playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Added"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "404", description = "No matching playlist for this user"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> addItem(@PathVariable Long playlistId,
                                     @Valid @RequestBody PlaylistItemRequest request,
                                     @AuthenticationPrincipal User user) {
        try {
            return toStatus(playlistService.addItem(
                    user.getUsername(), playlistId, request.mediaType(), request.tmdbId()));
        } catch (Exception e) {
            return serverError("Exception while adding playlist item", e);
        }
    }

    @DeleteMapping("/{playlistId}/items/{itemId}")
    @Operation(summary = "Remove a title from a playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Removed"),
            @ApiResponse(responseCode = "404", description = "No matching playlist/item for this user"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> removeItem(@PathVariable Long playlistId,
                                        @PathVariable Long itemId,
                                        @AuthenticationPrincipal User user) {
        try {
            return toStatus(playlistService.removeItem(user.getUsername(), playlistId, itemId));
        } catch (Exception e) {
            return serverError("Exception while removing playlist item", e);
        }
    }

    @PutMapping("/{playlistId}/items/order")
    @Operation(summary = "Reorder a playlist's items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reordered"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "404", description = "No matching playlist for this user"),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> reorder(@PathVariable Long playlistId,
                                     @Valid @RequestBody PlaylistReorderRequest request,
                                     @AuthenticationPrincipal User user) {
        try {
            return toStatus(playlistService.reorder(user.getUsername(), playlistId, request.itemIds()));
        } catch (Exception e) {
            return serverError("Exception while reordering playlist items", e);
        }
    }

    private ResponseEntity<?> toStatus(boolean found) {
        return ResponseEntity.status(found ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND).build();
    }

    private ResponseEntity<?> serverError(String message, Exception e) {
        LOGGER.log(Level.SEVERE, message, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(e.getMessage()));
    }
}
