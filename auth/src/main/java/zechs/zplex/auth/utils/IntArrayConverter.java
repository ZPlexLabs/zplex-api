package zechs.zplex.auth.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter
public class IntArrayConverter implements AttributeConverter<int[], String> {

    @Override
    public String convertToDatabaseColumn(int[] attribute) {
        if (attribute == null) return null;
        return Arrays.toString(attribute)
                .replaceAll("\\[|]|\\s", "");
    }

    @Override
    public int[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new int[0];
        return Arrays.stream(dbData.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
