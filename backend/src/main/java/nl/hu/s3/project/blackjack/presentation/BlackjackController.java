package nl.hu.s3.project.blackjack.presentation;

import jakarta.transaction.Transactional;
import nl.hu.s3.project.blackjack.application.GameService;
import nl.hu.s3.project.blackjack.domain.Game;
import nl.hu.s3.project.chips.ChipsController;
import nl.hu.s3.project.lobby.Player;
import nl.hu.s3.project.lobby.PlayerRepository;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/blackjack")
@Transactional
/**
 * Main REST controller for the Blackjack feature.
 * <p>
 * This class coordinates game persistence, chip transactions and HTTP response generation
 * to provide a complete and user-friendly Blackjack experience.
 */
public class BlackjackController {

    private final GameService gameService;
    private final PlayerRepository players;
    private final ChipsController chips;

    private Random random = new Random();

    public BlackjackController(GameService gameService, PlayerRepository players, ChipsController chips) {
        this.gameService = gameService;
        this.players = players;
        this.chips = chips;
    }

    @GetMapping
    public List<Link> home() {
        return List.of(Link.of("/games"));
    }

    @GetMapping("/games")
    public List<GameResponse> getAllGames(Authentication authentication) {
        return this.gameService.listGames(authentication.getName());
    }

    @GetMapping("/games/{id}")
    public ResponseEntity<GameResponse> getGame(Authentication authentication, @PathVariable Long id) {
        // Ownership is checked together with the database lookup for additional security.
        Game found = findUsersGame(authentication.getName(), id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response(found));
    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<GameResponse> cancelGame(Authentication authentication, @PathVariable Long id) {
        // A game can only be cancelled before the player has performed a move.
        Game found = findUsersGame(authentication.getName(), id);
        if (found == null || !gameService.isNew(found)) {
            return ResponseEntity.notFound().build();
        }
        GameResponse response = response(found);
        games.delete(found);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/games")
    public ResponseEntity<EntityModel<GameResponse>> startGame(Authentication authentication,
                                                                @RequestBody NewGame request) {
        // Construct a standard deck containing all 52 unique card identifiers.
        List<Integer> deck = new ArrayList<>();
        for (int card = 1; card <= 52; card++) {
            deck.add(card);
        }
        // Randomize the order to ensure a fair game experience.
        Collections.shuffle(deck, this.random);

        Player player = this.players.findById(authentication.getName());
        if(player == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player not found");
        }

        Game game = gameService.createGame(player, deck, request.bet());
        chips.withdrawChips(authentication.getName(), (long) request.bet());

        URI location = URI.create("/blackjack/games/" + game.getId());
        EntityModel<GameResponse> model = EntityModel.of(response(game),
                Link.of(location.toString(), IanaLinkRelations.SELF));
        return ResponseEntity.created(location).body(model);
    }

    @PostMapping("/games/{id}")
    public ResponseEntity<GameResponse> makeMove(Authentication authentication, @PathVariable Long id,
                                                  @RequestBody String move) {
        // Step 1: identify the user and retrieve their game.
        String username = authentication.getName();
        Game found = games.findUsersGame(id, username);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        Game game = found;

        // Step 2: execute the requested move using the appropriate game operation.
        switch (move) {
            case "Hit" -> gameService.hit(game);
            case "Stand" -> {
                gameService.dealerTurn(game);
            }
            case "DoubleDown" -> {
                if (gameService.isFinished(game)) throw new RuntimeException("Game already finished");
                chips.withdrawChips(username, (long) game.getBet());
                game.setBet(game.getBet() * 2);
                gameService.hit(game);
                if (!gameService.isFinished(game)) {
                    gameService.dealerTurn(game);
                }
            }
            default -> throw new IllegalArgumentException("Unknown move: " + move);
        }

        // Step 3: settle any payout and persist the latest game state.
        chips.depositChips(username, (long) gameService.payout(game));

        // Step 4: return the updated representation to the API consumer.
        GameResponse responseBody = response(game);
        return ResponseEntity.ok(responseBody);
    }

    private Game findUsersGame(String username, Long id) {
        return games.findUsersGame(id, username);
    }


    public static int calculatePayout(String result, int bet) {
        // Apply the standard payout multiplier for each possible result.
        return switch (result) {
            case "Surrendered" -> bet / 2;
            case "Push" -> bet;
            case "Blackjack" -> (int) (bet * 1.5);
            case "Won" -> bet * 2;
            default -> 0;
        };
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
}
