package nl.hu.s3.project.lobby;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/players")
/**
 * Controller for player management and authentication functionality.
 * This controller provides a RESTful API while keeping response construction in one place.
 */
public class PlayerController {
    private final PlayerRepository players;

    public PlayerController(PlayerRepository players) {
        this.players = players;
    }

    @GetMapping
    public List<PlayerResponse> getPlayers() {
        // Build response objects manually so persistence entities are not leaked to the API.
        List<PlayerResponse> result = new ArrayList<>();
        for (Player player : players.findAll()) {
            result.add(response(player));
        }
        return result;
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> addPlayer(@RequestBody PlayerResponse request) {
        // Validate all required input fields before accessing the database.
        validate(request.username(), request.name(), request.email());

        // Create and persist the new player.
        Player player = players.save(new Player(request.username(), request.name(), request.email()));
        URI location = URI.create("/players/" + request.username());
        PlayerResponse responseBody = response(player);
        return ResponseEntity.created(location).body(responseBody);
    }

    @GetMapping("/{name}")
    public PlayerResponse getPlayer(@PathVariable String name) {
        return response(find(name));
    }

    @PutMapping("/{name}")
    public PlayerResponse updatePlayer(@PathVariable String name, @RequestBody UpdatePlayer request) {
        // Validate first so invalid data is never passed to the repository.
        validate(name, request.name(), request.email());


        // Delegate the actual update operation to the data layer.
        Player player = players.updateDetails(name, request.name(), request.email());

        if (player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return response(player);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayer(@PathVariable String name) {
        players.delete(find(name));
    }

    private Player find(String username) {
        Player player = players.findById(username);
        if (player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return player;
    }

    private static void validate(String username, String name, String email) {
        // Perform explicit checks to provide user-friendly validation messages.
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is empty");
        }
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is empty");
        }
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is empty");
        }
    }

    public static PlayerResponse response(Player player) {
        PlayerResponse result = new PlayerResponse(
                player.getUsername(),
                player.getFullName(),
                player.getEmailAddress());
        return result;
    }

}
