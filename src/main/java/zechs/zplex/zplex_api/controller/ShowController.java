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
import zechs.zplex.zplex_api.model.PaginatedResponse;
import zechs.zplex.zplex_api.model.tv.Episode;
import zechs.zplex.zplex_api.model.tv.Season;
import zechs.zplex.zplex_api.model.tv.Show;
import zechs.zplex.zplex_api.service.ZPlexService;

import java.util.List;

@RestController
public class ShowController {

    private final ZPlexService zplexService;

    @Autowired
    public ShowController(ZPlexService zplexService) {
        this.zplexService = zplexService;
    }

    @GetMapping("/shows")
    public ResponseEntity<Object> getShows(
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

            List<Show> shows;
            int pageCount = 0;
            if (pageSize == 0) {
                shows = zplexService.getAllShows(query);
            } else {
                pageCount = zplexService.getShowsPageCount(query, pageSize);
                if (pageNumber > pageCount) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new ErrorResponse("Page number is greater than the total number of pages."));
                }
                shows = zplexService.getShows(query, pageSize, pageNumber);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PaginatedResponse<>(shows, pageNumber, pageCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/shows/{id}/seasons")
    public ResponseEntity<Object> getShowSeasons(@PathVariable("id") int showId) {
        try {
            List<Season> seasons = zplexService.getSeasons(showId);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(seasons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/shows/{id}/seasons/{seasonId}")
    public ResponseEntity<Object> getSeasonEpisodes(
            @PathVariable("id") int showId,
            @PathVariable("seasonId") int seasonId) {
        try {
            List<Episode> episodes = zplexService.getEpisodes(seasonId);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(episodes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}