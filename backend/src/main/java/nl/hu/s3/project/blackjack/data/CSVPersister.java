package nl.hu.s3.project.blackjack.data;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.hu.s3.project.blackjack.domain.Card;

import java.util.ArrayList;
import java.util.List;

@Converter
public class CSVPersister implements AttributeConverter<List<Card>, String> {
    @Override
    public String convertToDatabaseColumn(List<Card> attribute) {
        if (attribute == null) {
            return "";
        }
        List<String> strings = new ArrayList<>();
        for (Card i : attribute) {
            strings.add(i.toString());
        }

        return String.join(",", strings);
    }

    @Override
    public List<Card> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }

        List<Card> ints = new ArrayList<>();
        String[] parts = dbData.split(",");
        for (String p : parts) {
            ints.add(Card.parse(p));
        }
        return ints;
    }
}
