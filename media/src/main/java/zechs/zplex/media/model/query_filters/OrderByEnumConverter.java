package zechs.zplex.media.model.query_filters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class OrderByEnumConverter implements Converter<String, OrderBy> {

    @Override
    public OrderBy convert(String source) {
        try {
            return OrderBy.valueOf(source.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for OrderBy: " + source);
        }
    }
}