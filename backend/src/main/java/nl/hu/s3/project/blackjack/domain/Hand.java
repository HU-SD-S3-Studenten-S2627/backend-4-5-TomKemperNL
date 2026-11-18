package nl.hu.s3.project.blackjack.domain;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import nl.hu.s3.project.blackjack.data.CSVPersister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Hand {

    @Id
    @GeneratedValue
    private Long id;

    @Convert(converter = CSVPersister.class)
    private List<Card> cards = new ArrayList<>();

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public void add(Card card){
        this.cards.add(card);
    }

    public int getValue() {
        int total = 0;
        for(Card c : this.cards){
            total += c.baseValue();
        }

        return total;
    }

    public int size() {
        return this.cards.size();
    }
}
