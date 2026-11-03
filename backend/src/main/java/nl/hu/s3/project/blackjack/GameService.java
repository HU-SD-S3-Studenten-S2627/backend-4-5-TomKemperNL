package nl.hu.s3.project.blackjack;

import nl.hu.s3.project.lobby.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {
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
        game.setResult(BlackjackController.PLAYING);
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
        return !BlackjackController.PLAYING.equals(game.getResult());
    }

    public boolean isNew(Game game) {
        return game.getPlayerCards().size() == 2 && BlackjackController.PLAYING.equals(game.getResult());
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
}
