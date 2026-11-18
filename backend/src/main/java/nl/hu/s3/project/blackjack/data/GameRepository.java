package nl.hu.s3.project.blackjack.data;

import nl.hu.s3.project.blackjack.domain.Game;

import java.util.List;

public interface GameRepository {
    List<Game> findByUsername(String username);

    Game findById(Long id);

    Game findUsersGame(Long id, String username);

    Game save(Game game);

    void delete(Game game);
}
