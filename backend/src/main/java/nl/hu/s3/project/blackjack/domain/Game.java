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
    private List<Integer> playerCards;

    @Convert(converter = CSVPersister.class)
    private List<Integer> dealerCards;

    @Convert(converter = CSVPersister.class)
    private List<Integer> deck;

    private int bet;
    private String result;

    private int playerValue;

    protected Game() {
    }

    public Game(Player player, int bet){
        // Construct a standard deck containing all 52 unique card identifiers.
        List<Integer> deck = new ArrayList<>();
        for (int card = 1; card <= 52; card++) {
            deck.add(card);
        }
        // Randomize the order to ensure a fair game experience.
        Collections.shuffle(deck);

        this.setPlayer(player);
        this.setPlayerCards(new ArrayList<>());
        this.setDealerCards(new ArrayList<>());
        this.setDeck(new ArrayList<>(deck));
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


    private void addPlayerCard(int card) {
        this.getPlayerCards().add(card);

        int rank = ((card - 1) % 13) + 1;
        int cardValue;
        if (rank == 1) {
            cardValue = this.getPlayerValue() + 11 > 21 ? 1 : 11;
        } else {
            cardValue = Math.min(rank, 10);
        }
        this.setPlayerValue(this.getPlayerValue() + cardValue);
    }

    private void addDealerCard(int card) {
        this.getDealerCards().add(card);

    }

    private int takeCard() {
        return this.getDeck().removeFirst();
    }


    public Long getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Integer> getPlayerCards() {
        return playerCards;
    }

    public void setPlayerCards(List<Integer> playerCards) {
        this.playerCards = playerCards;
    }

    public List<Integer> getDealerCards() {
        return dealerCards;
    }

    public void setDealerCards(List<Integer> dealerCards) {
        this.dealerCards = dealerCards;
    }

    public List<Integer> getDeck() {
        return deck;
    }

    public void setDeck(List<Integer> deck) {
        this.deck = deck;
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
}
