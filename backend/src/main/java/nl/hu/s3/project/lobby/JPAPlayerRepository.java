package nl.hu.s3.project.lobby;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
/**
 * Repository providing CRUD operations for players.
 * The implementation uses EntityManager directly to provide full control over persistence.
 */
public class JPAPlayerRepository implements PlayerRepository {
    private final EntityManager entities;

    public JPAPlayerRepository(EntityManager entities) {
        this.entities = entities;
    }

    @Override
    public List<Player> findAll() {
        List<Player> foundPlayers = entities
                .createQuery("SELECT p FROM Player p", Player.class)
                .getResultList();
        return foundPlayers;
    }

    @Override
    public Player findById(String username) {
        Player foundPlayer = entities.find(Player.class, username);
        return foundPlayer;
    }

    @Override
    public boolean existsById(String username) {
        // Reuse EntityManager.find for an efficient primary-key lookup.
        Player existingPlayer = entities.find(Player.class, username);
        return existingPlayer != null;
    }

    @Override
    public Player save(Player player) {
        // Player usernames are assigned by the client, therefore merge supports new and existing players.
        return entities.merge(player);
    }

    @Override
    public void delete(Player player) {
        entities.remove(entities.contains(player) ? player : entities.merge(player));
    }

    @Override
    public Player updateDetails(String username, String fullName, String emailAddress) {
        // Locate the player before applying the requested changes.
        Player player = entities.find(Player.class, username);
        if (player == null) {
            return null;
        }
        // Update both fields together to keep the player's profile consistent.
        player.setFullName(fullName);
        player.setEmailAddress(emailAddress);

        // No explicit merge is required here because the entity is managed by JPA.
        return player;
    }
}
