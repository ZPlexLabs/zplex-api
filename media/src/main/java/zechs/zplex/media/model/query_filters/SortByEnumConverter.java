package zechs.zplex.media.model.query_filters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SortByEnumConverter implements Converter<String, SortBy> {

    @Override
    public SortBy convert(String source) {
        try {
            return SortBy.valueOf(source.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for SortBy: " + source);
        }
    }
}