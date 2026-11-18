package nl.hu.s3.project.blackjack.domain;

import java.util.HashMap;
import java.util.Map;

public enum Suit {
    Hearts, //harten
    Spades, //schoppen
    Diamonds, //ruiten
    Clubs; //klaveren

    @Override
    public String toString(){
        return switch (this){
            /*
            Te onleesbaar, spade & clubs zijn echt niet te doen:)
            case Hearts -> "♥";
            case Spades -> "♠";
            case Diamonds -> "◆";
            case Clubs -> "♣";*/
            case Hearts -> "H";
            case Spades -> "S";
            case Diamonds -> "D";
            case Clubs -> "C";
        };
    }

    private static final Map<String, Suit> mapping = new HashMap<>();

    static {
        for (Suit s : Suit.values()) {
            mapping.put(s.toString(), s);
        }
    }

    public static Suit parse(String s) {
        Suit result = mapping.get(s);
        if (result == null) {
            throw new RuntimeException("Unable to parse Suit " + s);
        }
        return result;
    }

}
