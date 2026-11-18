package nl.hu.s3.project.blackjack.domain;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import nl.hu.s3.project.blackjack.data.CSVPersister;
import nl.hu.s3.project.lobby.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Entity
public class Game {
    public static final String PLAYING = "Playing";

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Player player;

    @Convert(converter = CSVPersister.class)
    private List<Card> playerCards;

    @Convert(converter = CSVPersister.class)
    private List<Card> dealerCards;

    @Convert(converter = CSVPersister.class)
    private List<Card> deck;

    private int bet;
    private String result;

    private int playerValue;

    protected Game() {
    }

    public Game(Player player, int bet){
        // Construct a standard deck containing all 52 unique card identifiers.
        List<Card> deck = Card.deck();
        // Randomize the order to ensure a fair game experience.
        Collections.shuffle(deck);

        this.player = player;
        this.playerCards = new ArrayList<>();
        this.dealerCards = new ArrayList<>();
        this.deck = new ArrayList<>(deck);
        this.setBet(bet);
        this.setResult(PLAYING);
        this.setPlayerValue(0);

        addPlayerCard(takeCard());
        addPlayerCard(takeCard());
        addDealerCard(takeCard());
        addDealerCard(takeCard());

        if (this.getPlayerValue() == 21) {
            this.setResult("Blackjack");
        }

    }


    private void addPlayerCard(Card card) {
        this.getPlayerCards().add(card);
        this.setPlayerValue(this.getPlayerValue() + card.baseValue());
    }

    private void addDealerCard(Card card) {
        this.getDealerCards().add(card);
    }

    private Card takeCard() {
        return this.getDeck().removeFirst();
    }


    public Long getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Card> getPlayerCards() {
        return playerCards;
    }

    public List<Card> getDealerCards() {
        return dealerCards;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getPlayerValue() {
        return playerValue;
    }

    public void setPlayerValue(int playerValue) {
        this.playerValue = playerValue;
    }


    public void hit() {
        makeSureGameIsStillRunning();
        addPlayerCard(takeCard());
        if (this.getPlayerValue() > 21) {
            this.setResult("Bust");
        }
    }

    public void stand() {
        makeSureGameIsStillRunning();
        int value = 0;
        for (Card card : this.getDealerCards()) {
            value += card.baseValue();
        }

        // A dealer always stands on a hand value of 17.
        while (value < 17) {
            Card newCard = takeCard();
            addDealerCard(newCard);
            value += newCard.baseValue();
        }

        int playerValue = this.getPlayerValue();
        int dealerValue = value;
        if (dealerValue > 21 || playerValue > dealerValue) {
            this.setResult("Won");
        }else if (playerValue == dealerValue) {
            this.setResult("Push");
        } else {
            this.setResult("Lost");
        }
    }

    private void makeSureGameIsStillRunning() {
        if (this.isFinished()) {
            throw new RuntimeException("Game already finished");
        }
    }

    public boolean isFinished() {
        return !PLAYING.equals(this.getResult());
    }


    public void doubleDown() {
        if (this.isFinished()) throw new RuntimeException("Game already finished");
        this.setBet(this.getBet() * 2);
        this.hit();
        if (!this.isFinished()) {
            this.stand();
        }
    }
}
