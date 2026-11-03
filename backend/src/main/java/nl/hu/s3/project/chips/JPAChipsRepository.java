package nl.hu.s3.project.chips;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
/**
 * Data access component for chip balances.
 * This class encapsulates database access so that other parts of the application do not
 * need to know how chip balances are persisted internally.
 */
public class JPAChipsRepository implements ChipsRepository {
    private final EntityManager entities;

    public JPAChipsRepository(EntityManager entities) {
        this.entities = entities;
    }

    @Override
    public ChipsAccount findByUsername(String username) {
        // Query by username because the generated technical id is not known to API consumers.
        List<ChipsAccount> result = entities.createQuery(
                        "SELECT c FROM ChipsAccount c WHERE c.username = :username", ChipsAccount.class)
                .setParameter("username", username)
                .getResultList();
        if (result.isEmpty()) {
            return null;
        }
        // Usernames should be unique. In production this should also be enforced in the database.
        return result.getFirst();
    }

    @Override
    public ChipsAccount findOrCreate(String username) {
        // First try to locate the existing balance for the user.
        ChipsAccount found = findByUsername(username);
        if (found != null) {
            return found;
        }
        // Every user starts with zero chips by default.
        // The new entity will be persisted when an actual balance change occurs.
        ChipsAccount newOne = new ChipsAccount(username, 0L);
        this.entities.persist(newOne);
        return newOne;
    }
}
