package nl.hu.s3.project.blackjack.domain;

import java.util.HashMap;
import java.util.Map;

public enum Rank {
    Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten,
    Jack, Queen, King,
    Ace;

    public int getValue() {
        return switch (this) {
            case Two -> 2;
            case Three -> 3;
            case Four -> 4;
            case Five -> 5;
            case Six -> 6;
            case Seven -> 7;
            case Eight -> 8;
            case Nine -> 9;
            case Ten, Jack, Queen, King -> 10;
            case Ace -> 11;
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case Two, Three, Four, Five, Six, Seven, Eight, Nine -> String.valueOf(this.getValue());
            case Ten -> "X";
            case Jack -> "J";
            case Queen -> "Q";
            case King -> "K";
            case Ace -> "A";
        };
    }

    private static final Map<String, Rank> mapping = new HashMap<>();

    static {
        for (Rank r : Rank.values()) {
            mapping.put(r.toString(), r);
        }
    }

    public static Rank parse(String s) {
        Rank result = mapping.get(s);
        if (result == null) {
            throw new RuntimeException("Unable to parse Rank " + s);
        }
        return result;
    }

    public static final int ACE_LOW_VALUE = 1;
}
