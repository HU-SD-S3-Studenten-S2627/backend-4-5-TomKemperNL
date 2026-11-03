package nl.hu.s3.project.chips;

import java.time.LocalDateTime;

/**
 * Immutable response object containing the current chip balance for a user.
 */
public record Balance(String username, LocalDateTime lastUpdate, Long chips) {
}
