package zechs.zplex.suggestions.controller;

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
import zechs.zplex.common.model.ErrorResponse;
import zechs.zplex.suggestions.model.SearchSuggestion;
import zechs.zplex.suggestions.model.Suggestion;
import zechs.zplex.suggestions.service.SuggestionService;

import java.util.List;

@RestController
@RequestMapping("/api/suggestion")
public class SuggestionsController {

    private static final int SUGGESTION_LIMIT = 25;
    private final SuggestionService suggestionService;

    public SuggestionsController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @GetMapping("/search")
    @Operation(summary = "Get search suggestions for the day")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully fetched " + SUGGESTION_LIMIT + " search suggestions of the day.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SearchSuggestion.class))
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
    public ResponseEntity<?> getSearchSuggestions() {
        try {
            List<SearchSuggestion> searchSuggestions = suggestionService.getSearchSuggestions(SUGGESTION_LIMIT);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(searchSuggestions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("")
    @Operation(summary = "Get suggestions for the day")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully fetched " + SUGGESTION_LIMIT + " suggestions of the day.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Suggestion.class))
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
    public ResponseEntity<?> getSuggestions() {
        try {
            List<Suggestion> searchSuggestions = suggestionService.getSuggestions(SUGGESTION_LIMIT);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(searchSuggestions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

}