package zechs.zplex.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;


@RestController
public class HealthController {

    private static final Logger logger = Logger.getLogger(HealthController.class.getName());

    @GetMapping("/health")
    public ResponseEntity<Void> getHealth() {
        logger.log(Level.INFO, "Health check endpoint called - status=UP, endpoint=/health, service={0}",
                HealthController.class.getSimpleName());
        return ResponseEntity.ok().build();
    }

}