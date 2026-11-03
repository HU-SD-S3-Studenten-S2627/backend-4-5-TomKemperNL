package nl.hu.s3.project.chips;

import jakarta.validation.constraints.Positive;

/**
 * Request object for operations that modify a chip balance by a specific amount.
 */
public record Amount(@Positive Long amount) {
}
