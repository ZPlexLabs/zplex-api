package zechs.zplex.suggestions.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import zechs.zplex.suggestions.model.SearchSuggestion;
import zechs.zplex.suggestions.model.Suggestion;
import zechs.zplex.suggestions.repository.SuggestionsRepository;

import java.util.List;
import java.util.logging.Logger;

@Service
public class SuggestionService {

    private static final Logger logger = Logger.getLogger(SuggestionService.class.getName());
    private final SuggestionsRepository repository;

    public SuggestionService(SuggestionsRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "suggestions", key = "#count", unless = "#result == null || #result.isEmpty()")
    public List<Suggestion> getSuggestions(int count) {
        logger.info("Cache miss for suggestions with count=" + count);
        return repository.getSuggestions(count);
    }

    @Cacheable(value = "searchSuggestions", key = "#count", unless = "#result == null || #result.isEmpty()")
    public List<SearchSuggestion> getSearchSuggestions(int count) {
        logger.info("Cache miss for search suggestions with count=" + count);
        return repository.getSearchSuggestions(count);
    }
}