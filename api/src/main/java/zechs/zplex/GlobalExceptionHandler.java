package zechs.zplex;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import zechs.zplex.common.model.ErrorResponse;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception exception) {
        logger.log(Level.SEVERE, "Unexpected error occurred", exception);
        return switch (exception) {
            case NoResourceFoundException ex -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("The requested resource was not found."));

            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("Something went wrong. Please try again later."));
        };
    }

    @ExceptionHandler(exception = {DataAccessException.class, SQLException.class})
    public ResponseEntity<?> handleDatabaseExceptions(DataAccessException ex) {
        logger.log(Level.WARNING, "Database error", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("Database temporarily unavailable."));
    }

}
