package zechs.zplex.config.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zechs.zplex.common.capability.Capabilities;
import zechs.zplex.common.capability.Capability;
import zechs.zplex.common.model.ErrorResponse;
import zechs.zplex.common.utils.AESUtil;
import zechs.zplex.config.model.ConfigResponse;
import zechs.zplex.config.model.GoogleServiceAccount;
import zechs.zplex.config.service.FilterConfigService;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private static final Logger LOGGER = Logger.getLogger(ConfigController.class.getName());
    private final FilterConfigService configService;
    private final Gson gson;

    public ConfigController(FilterConfigService configService, Gson gson) {
        this.configService = configService;
        this.gson = gson;
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
    public ResponseEntity<?> getConfigs(HttpServletRequest request) {
        try {
            String requestToken = request.getHeader("Authorization").substring(7)
                    .trim();

            ConfigResponse config = new ConfigResponse();

            GoogleServiceAccount serviceAccount = new GoogleServiceAccount();
            serviceAccount.setClientId(System.getenv("GOOGLE_DRIVE_CLIENT_ID"));
            serviceAccount.setClientEmail(System.getenv("GOOGLE_DRIVE_CLIENT_EMAIL"));
            serviceAccount.setPrivateKeyPkcs8(System.getenv("GOOGLE_DRIVE_PRIVATE_KEY_PKCS8"));
            serviceAccount.setPrivateKeyId(System.getenv("GOOGLE_DRIVE_PRIVATE_KEY_ID"));

            String encryptedServiceAccount = AESUtil.encrypt(gson.toJson(serviceAccount), requestToken);
            config.setServiceAccount(encryptedServiceAccount);

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
