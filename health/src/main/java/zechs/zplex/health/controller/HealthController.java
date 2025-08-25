package zechs.zplex.health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;


@RestController
@Tag(name = "Health", description = "API health check endpoints")
public class HealthController {

    private static final Logger logger = Logger.getLogger(HealthController.class.getName());

    @Operation(
            summary = "Health Check",
            description = "Returns `200 OK` if the service is healthy",
            responses = {@ApiResponse(responseCode = "200", description = "Service is healthy")}
    )
    @GetMapping("/health")
    public ResponseEntity<Void> getHealth() {
        logger.log(Level.INFO, "Health check endpoint called - status=UP, endpoint=/health, service={0}",
                HealthController.class.getSimpleName());
        return ResponseEntity.ok().build();
    }

}