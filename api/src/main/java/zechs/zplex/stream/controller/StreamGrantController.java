package zechs.zplex.stream.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zechs.zplex.auth.model.User;
import zechs.zplex.stream.model.StreamGrantResponse;
import zechs.zplex.stream.service.StreamGrantService;

@RestController
@RequestMapping("/api/stream")
public class StreamGrantController {

    private final StreamGrantService streamGrantService;

    public StreamGrantController(StreamGrantService streamGrantService) {
        this.streamGrantService = streamGrantService;
    }

    @GetMapping("/grant/{fileId}")
    @Operation(summary = "Create a short-lived stream grant for an accessible file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stream grant created"),
            @ApiResponse(responseCode = "403", description = "Streaming is not allowed"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<?> createGrant(@PathVariable String fileId,
                                         @AuthenticationPrincipal User user) {
        try {
            StreamGrantResponse response = streamGrantService.createGrant(user, fileId);
            return ResponseEntity.ok(response);
        } catch (StreamGrantService.StreamAccessDeniedException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
