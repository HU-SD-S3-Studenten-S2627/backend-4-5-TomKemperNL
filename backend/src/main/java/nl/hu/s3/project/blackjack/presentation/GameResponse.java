package nl.hu.s3.project.blackjack.presentation;

import java.util.List;

/**
 * Response object used to expose the current state of a Blackjack game to API consumers.
 */
public record GameResponse(long id, List<String> playerHand, List<String> dealerHand,
                           int bet, String result) {
}
