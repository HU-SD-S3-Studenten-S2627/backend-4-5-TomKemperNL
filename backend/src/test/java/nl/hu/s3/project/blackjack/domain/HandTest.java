package nl.hu.s3.project.blackjack.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandTest {

    @Test
    public void simpleValue() {
        Hand hand = new Hand(List.of(new Card(Suit.Diamonds, Rank.Four), new Card(Suit.Clubs, Rank.King)));
        assertEquals(14, hand.getValue());
    }

    @Test
    public void highAce() {
        Hand hand = new Hand(List.of(new Card(Suit.Diamonds, Rank.Four), new Card(Suit.Clubs, Rank.Ace)));
        assertEquals(15, hand.getValue());
    }
}