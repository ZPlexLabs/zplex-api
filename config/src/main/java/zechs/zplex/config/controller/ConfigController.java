package zechs.zplex.config.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zechs.zplex.common.capability.Capabilities;
import zechs.zplex.common.capability.Capability;
import zechs.zplex.common.model.ErrorResponse;
import zechs.zplex.config.model.ConfigResponse;
import zechs.zplex.config.service.FilterConfigService;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private static final Logger LOGGER = Logger.getLogger(ConfigController.class.getName());
    private final FilterConfigService configService;

    public ConfigController(FilterConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/capabilities")
    @Operation(summary = "Get list of all the capabilities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched list of capabilities",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Capability.class))
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> getAllCapabilities() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Capabilities.getAllCapabilities());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }


    @GetMapping("")
    @Operation(summary = "Get all required configs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully fetched all required configurations",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConfigResponse.class)
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
    public ResponseEntity<?> getConfigs() {
        try {
            ConfigResponse config = new ConfigResponse();
            config.setStreamingHost(System.getenv().getOrDefault("ZPLEX_STREAM_HOST", ""));
            config.addFilter(configService.getFilterConfig(zechs.zplex.common.model.MediaType.SHOW));
            config.addFilter(configService.getFilterConfig(zechs.zplex.common.model.MediaType.MOVIE));

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(config);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

}
