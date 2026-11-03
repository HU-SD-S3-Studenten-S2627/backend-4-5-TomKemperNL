package nl.hu.s3.project.lobby;

import java.util.List;

public interface PlayerRepository {
    List<Player> findAll();

    Player findById(String username);

    default boolean existsById(String username){
        return this.findById(username) != null;
    }

    Player save(Player player);

    void delete(Player player);

    Player updateDetails(String name, String name1, String email);
}
