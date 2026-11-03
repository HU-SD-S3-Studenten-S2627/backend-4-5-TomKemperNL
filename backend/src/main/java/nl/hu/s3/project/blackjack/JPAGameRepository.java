package nl.hu.s3.project.blackjack;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
/**
 * Repository class responsible for all database operations related to Blackjack games.
 * <p>
 * By centralizing the persistence logic in this class, the application maintains a clean
 * separation of concerns and makes the code easier to maintain and test.
 */
public class JPAGameRepository implements GameRepository {
    private final EntityManager entities;

    public JPAGameRepository(EntityManager entities) {
        this.entities = entities;
    }

    @Override
    public List<Game> findByUsername(String username) {
        // Use a parameterized JPQL query to prevent SQL injection and improve security.
        List<Game> foundGames = entities.createQuery(
                        "SELECT g FROM Game g WHERE g.player.username = :username", Game.class)
                .setParameter("username", username)
                .getResultList();

        // Return the list to the calling layer. The list can be empty, which is a valid result.
        return foundGames;
    }

    @Override
    public Game findById(Long id) {
        // EntityManager returns null when the entity does not exist.
        return entities.find(Game.class, id);
    }

    @Override
    public Game findUsersGame(Long id, String username) {
        // Both the id and username are checked here for an additional layer of security.
        // In a production application, authorization could also be handled by Spring Security.
        List<Game> result = entities.createQuery(
                        "SELECT g FROM Game g WHERE g.id = :id AND g.player.username = :username",
                        Game.class)
                .setParameter("id", id)
                .setParameter("username", username)
                .getResultList();
        if (result.isEmpty()) {
            // No game was found for the supplied combination.
            return null;
        }
        // IDs are unique, so the first result is the expected result.
        return result.getFirst();
    }

    @Override
    public Game save(Game game) {
        // New entities do not have an id yet and must be persisted instead of merged.
        if (game.getId() == null) {
            entities.persist(game);
            return game;
        }
        // Merge ensures that detached entities can also be updated correctly.
        return entities.merge(game);
    }

    @Override
    public void delete(Game game) {
        // EntityManager can only remove managed entities, so merge when necessary.
        entities.remove(entities.contains(game) ? game : entities.merge(game));
    }

}
