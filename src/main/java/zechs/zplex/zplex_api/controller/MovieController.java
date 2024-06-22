package zechs.zplex.zplex_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zechs.zplex.zplex_api.model.ErrorResponse;
import zechs.zplex.zplex_api.model.Movie;
import zechs.zplex.zplex_api.model.PaginatedResponse;
import zechs.zplex.zplex_api.service.ZPlexService;

import java.util.List;

@RestController
public class MovieController {

    private final ZPlexService zplexService;

    @Autowired
    public MovieController(ZPlexService zplexService) {
        this.zplexService = zplexService;
    }

    @GetMapping("/movies")
    public ResponseEntity<Object> getMovies(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber
    ) {
        try {
            if (pageSize < 0 || pageNumber <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ErrorResponse("Page size must be greater than or equal to zero, and page number must be greater than zero."));
            }

            List<Movie> movies;
            int pageCount = 0;
            if (pageSize == 0) {
                movies = zplexService.getAllMovies(query);
            } else {
                pageCount = zplexService.getMoviesPageCount(query, pageSize);
                if (pageNumber > pageCount) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new ErrorResponse("Page number is greater than the total number of pages."));
                }
                movies = zplexService.getMovies(query, pageSize, pageNumber);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PaginatedResponse<>(movies, pageNumber, pageCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<Object> getMovieById(@PathVariable("id") int movieId) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(zplexService.getMovieById(movieId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

}