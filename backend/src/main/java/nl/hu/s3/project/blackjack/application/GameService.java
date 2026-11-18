package nl.hu.s3.project.blackjack.application;

import nl.hu.s3.project.blackjack.domain.Game;
import nl.hu.s3.project.blackjack.data.GameRepository;
import nl.hu.s3.project.blackjack.presentation.BlackjackController;
import nl.hu.s3.project.blackjack.presentation.GameResponse;
import nl.hu.s3.project.lobby.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    public static final String PLAYING = "Playing";
    public static final String CARD_SEPARATOR = ",";
    public static final String HIDDEN_CARD = "**";

    private GameRepository games;

    public GameService(GameRepository games) {
        this.games = games;
    }

    public Game createGame(Player player, List<Integer> cards, int bet) {
        Game game = new Game();
        game.setPlayer(player);
        game.setPlayerCards(new ArrayList<>());
        game.setDealerCards(new ArrayList<>());
        game.setDeck(new ArrayList<>(cards));
        game.setBet(bet);
        game.setResult(PLAYING);
        game.setPlayerValue(0);

        addPlayerCard(game, takeCard(game));
        addPlayerCard(game, takeCard(game));
        addDealerCard(game, takeCard(game));
        addDealerCard(game, takeCard(game));

        if (game.getPlayerValue() == 21) {
            game.setResult("Blackjack");
        }

        this.games.save(game);
        return game;
    }

    public void hit(Game game) {
        makeSureGameIsStillRunning(game);
        addPlayerCard(game, takeCard(game));
        if (game.getPlayerValue() > 21) {
            game.setResult("Bust");
        }
    }

    public void dealerTurn(Game game) {
        makeSureGameIsStillRunning(game);
        int value = 0;
        for (int card : game.getDealerCards()) {
            int rank = ((card - 1) % 13) + 1;
            value += rank == 1 ? 1 : Math.min(rank, 10);
        }

        // A dealer always stands on a hand value of 17.
        while (value < 17) {
            int newCard = takeCard(game);
            addDealerCard(game, newCard);
            int rank = ((newCard - 1) % 13) + 1;
            value += rank == 1 ? 1 : Math.min(rank, 10);
        }

        int playerValue = game.getPlayerValue();
        int dealerValue = value;
        if (dealerValue > 21 || playerValue > dealerValue) {
            game.setResult("Won");
        }else if (playerValue == dealerValue) {
            game.setResult("Push");
        } else {
            game.setResult("Lost");
        }
    }

    public int payout(Game game) {
        return BlackjackController.calculatePayout(game.getResult(), game.getBet());
    }

    public boolean isFinished(Game game) {
        return !PLAYING.equals(game.getResult());
    }

    public boolean isNew(Game game) {
        return game.getPlayerCards().size() == 2 && PLAYING.equals(game.getResult());
    }

    private void makeSureGameIsStillRunning(Game game) {
        if (isFinished(game)) {
            throw new RuntimeException("Game already finished");
        }
    }

    private int takeCard(Game game) {
        return game.getDeck().removeFirst();
    }

    private void addPlayerCard(Game game, int card) {
        game.getPlayerCards().add(card);

        int rank = ((card - 1) % 13) + 1;
        int cardValue;
        if (rank == 1) {
            cardValue = game.getPlayerValue() + 11 > 21 ? 1 : 11;
        } else {
            cardValue = Math.min(rank, 10);
        }
        game.setPlayerValue(game.getPlayerValue() + cardValue);
    }

    private void addDealerCard(Game game, int card) {
        game.getDealerCards().add(card);

    }

    public List<GameResponse> listGames(String name) {
        // Convert all games owned by the current user to safe response objects.
        List<GameResponse> result = new ArrayList<>();
        for (Game game : games.findByUsername(name)) {
            result.add(response(game));
        }
        return result;
    }


    private GameResponse response(Game game) {
        // Translate the internal numeric representation into human-readable card names.
        List<String> player = new ArrayList<>();
        for (Integer card : game.getPlayerCards()) {
            player.add(cardName(card));
        }
        List<String> dealer = new ArrayList<>();
        for (Integer card : game.getDealerCards()) {
            dealer.add(cardName(card));
        }
        if (!this.isFinished(game)) {
            // Hide the dealer's hole card while the game is still active.
            dealer.set(0, HIDDEN_CARD);
        }
        return new GameResponse(game.getId(), player, dealer, game.getBet(), game.getResult());
    }

    private static String cardName(int card) {
        String suit = switch ((card - 1) / 13) {
            case 0 -> "H";
            case 1 -> "S";
            case 2 -> "D";
            default -> "C";
        };
        int rank = ((card - 1) % 13) + 1;
        String rankName = switch (rank) {
            case 1 -> "A";
            case 10 -> "X";
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            default -> Integer.toString(rank);
        };
        return suit + rankName;
    }

}
