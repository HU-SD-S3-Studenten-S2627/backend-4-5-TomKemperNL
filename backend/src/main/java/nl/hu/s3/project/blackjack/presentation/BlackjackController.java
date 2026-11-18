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

import static nl.hu.s3.project.lobby.PlayerController.response;

@RestController
@RequestMapping("/blackjack")
/**
 * Main REST controller for the Blackjack feature.
 * <p>
 * This class coordinates game persistence, chip transactions and HTTP response generation
 * to provide a complete and user-friendly Blackjack experience.
 */
public class BlackjackController {

    private final GameService gameService;
    private final ChipsController chips;

    public BlackjackController(GameService gameService, ChipsController chips) {
        this.gameService = gameService;
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
        GameResponse found = findUsersGame(authentication.getName(), id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<GameResponse> cancelGame(Authentication authentication, @PathVariable Long id) {
        // A game can only be cancelled before the player has performed a move.
        GameResponse found = findUsersGame(authentication.getName(), id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        if (!gameService.isNew(authentication.getName(), id)){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        gameService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/games")
    public ResponseEntity<EntityModel<GameResponse>> startGame(Authentication authentication,
                                                                @RequestBody NewGame request) {

        GameResponse game = gameService.createGame(authentication.getName(), request.bet());
        chips.withdrawChips(authentication.getName(), (long) request.bet());

        URI location = URI.create("/blackjack/games/" + game.id());
        EntityModel<GameResponse> model = EntityModel.of(game,
                Link.of(location.toString(), IanaLinkRelations.SELF));
        return ResponseEntity.created(location).body(model);
    }

    @PostMapping("/games/{id}")
    public ResponseEntity<GameResponse> makeMove(Authentication authentication, @PathVariable Long id,
                                                  @RequestBody String move) {
        // Step 1: identify the user and retrieve their game.
        String username = authentication.getName();
        GameResponse found = findUsersGame(username, id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }

        // Step 2: execute the requested move using the appropriate game operation.
        GameResponse responseBody = this.gameService.makeMove(username, id, move);

        // Step 3: settle any payout and persist the latest game state.
        chips.depositChips(username, (long) gameService.payout(username, id));

        // Step 4: return the updated representation to the API consumer.
        return ResponseEntity.ok(responseBody);
    }

    private GameResponse findUsersGame(String username, Long id) {
        return gameService.findUsersGame(username, id);
    }

}
