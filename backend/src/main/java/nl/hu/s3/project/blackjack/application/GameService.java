package nl.hu.s3.project.blackjack.application;

import jakarta.transaction.Transactional;
import nl.hu.s3.project.blackjack.domain.Game;
import nl.hu.s3.project.blackjack.data.GameRepository;
import nl.hu.s3.project.blackjack.presentation.GameResponse;
import nl.hu.s3.project.chips.ChipsAccount;
import nl.hu.s3.project.chips.ChipsRepository;
import nl.hu.s3.project.lobby.Player;
import nl.hu.s3.project.lobby.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static nl.hu.s3.project.blackjack.domain.Game.PLAYING;

@Service
@Transactional
public class GameService {


    public static final String CARD_SEPARATOR = ",";
    public static final String HIDDEN_CARD = "**";

    private GameRepository games;
    private final PlayerRepository players;
    private final ChipsRepository chips;

    private Random random = new Random();

    public GameService(GameRepository games, PlayerRepository players, ChipsRepository chips) {
        this.games = games;
        this.players = players;
        this.chips = chips;
    }
    public int payout(String username, Long id) {
        Game game = this.games.findUsersGame(id, username);
        return calculatePayout(game.getResult(), game.getBet());
    }

    public boolean isNew(String name, Long id) {
        Game game = this.games.findUsersGame(id, name);
        return game.getPlayerCards().size() == 2 && PLAYING.equals(game.getResult());
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
        if (!game.isFinished()) {
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

    public void delete(String name, Long id) {
    }

    public GameResponse createGame(String name, int bet) {
        // Construct a standard deck containing all 52 unique card identifiers.
        List<Integer> deck = new ArrayList<>();
        for (int card = 1; card <= 52; card++) {
            deck.add(card);
        }
        // Randomize the order to ensure a fair game experience.
        Collections.shuffle(deck, this.random);

        Player player = this.players.findById(name);
        if(player == null){
            throw new IllegalArgumentException("Player not found");
        }


        Game game = new Game(player, bet);
        this.games.save(game);
        return response(game);
    }

    public GameResponse makeMove(String username, Long id, String move) {

        Game game = this.games.findUsersGame(id, username);

        switch (move) {
            case "Hit" -> game.hit();
            case "Stand" -> {
                game.stand();
            }
            case "DoubleDown" -> {
                if (game.isFinished()) throw new RuntimeException("Game already finished");
                ChipsAccount account =  chips.findByUsername(username);
                if(account == null){
                    throw new IllegalArgumentException("Chips not found");
                }
                account.withdraw((long) game.getBet());
                game.setBet(game.getBet() * 2);
                game.hit();
                if (!game.isFinished()) {
                    game.stand();
                }
            }
            default -> throw new IllegalArgumentException("Unknown move: " + move);
        }

        return null;
    }


    public GameResponse findUsersGame(String username, Long id) {
        return response(this.games.findUsersGame(id, username));
    }



    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    private static int calculatePayout(String result, int bet) {
        // Apply the standard payout multiplier for each possible result.
        return switch (result) {
            case "Surrendered" -> bet / 2;
            case "Push" -> bet;
            case "Blackjack" -> (int) (bet * 1.5);
            case "Won" -> bet * 2;
            default -> 0;
        };
    }
}
