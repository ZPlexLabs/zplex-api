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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zechs.zplex.auth.model.User;
import zechs.zplex.common.model.ErrorResponse;
import zechs.zplex.userdata.model.api.ContinueWatchingItem;
import zechs.zplex.userdata.model.api.ProgressUpdateRequest;
import zechs.zplex.userdata.service.WatchProgressService;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private static final Logger LOGGER = Logger.getLogger(MeController.class.getName());

    private final WatchProgressService watchProgressService;

    public MeController(WatchProgressService watchProgressService) {
        this.watchProgressService = watchProgressService;
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
}
