package nl.hu.s3.project.blackjack;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class CSVPersister implements AttributeConverter<List<Integer>, String> {
    @Override
    public String convertToDatabaseColumn(List<Integer> attribute) {
        if (attribute == null) {
            return "";
        }
        List<String> strings = new ArrayList<>();
        for (Integer i : attribute) {
            strings.add(i.toString());
        }

        return String.join(",", strings);
    }

    @Override
    public List<Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }

        List<Integer> ints = new ArrayList<>();
        String[] parts = dbData.split(",");
        for (String p : parts) {
            ints.add(Integer.parseInt(p));
        }
        return ints;
    }
}
