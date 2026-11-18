package nl.hu.s3.project.blackjack.domain;

import java.util.ArrayList;
import java.util.List;

public class Card {
    private Rank rank;
    private Suit suit;

    public Card(Suit suit, Rank rank) {
        this.rank = rank;
        this.suit = suit;
    }

    public int baseValue() {
        return rank.getValue();
    }

    @Override
    public String toString() {
        return String.format("%s%s", this.suit, this.rank);
    }

    public static Card parse(String cardString) {
        if (cardString.length() != 2) {
            throw new RuntimeException("Unable to parse card " + cardString);
        }

        Suit suit = Suit.parse(cardString.substring(0, 1));
        Rank rank = Rank.parse(cardString.substring(1, 2));
        return new Card(suit, rank);
    }

    public static String toStringMany(List<Card> cards) {
        return String.join("", cards.stream().map(Card::toString).toList());
    }

    public static List<Card> parseMany(String cardsString) {
        cardsString = cardsString.replace(" ", "").trim();

        if (cardsString.length() % 2 != 0) {
            throw new RuntimeException(String.format("Unable to parse %s, uneven length", cardsString));
        }

        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < cardsString.length(); i = i + 2) {
            String cardString = cardsString.substring(i, i + 2);
            cards.add(Card.parse(cardString));
        }

        return cards;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public static List<Card> deck(){
        List<Card> result = new ArrayList<>();
        for(Suit s: Suit.values()){
            for(Rank r: Rank.values()){
                result.add(new Card(s,r));
            }
        }
        return result;
    }
}
