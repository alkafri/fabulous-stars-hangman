package yh.fabulousstars.hangman.client;

import java.util.List;

public interface IGame {

    /**
     * Get game id.
     *
     * @return Game id
     */
    String getId();

    /**
     * Get manager the game belongs to.
     * @return IGameManager instance
     */
    IGameManager getManager();

    /**
     * Get game name.
     * @return game name
     */
    String getName();

    /**
     * Get game theme.
     * @return theme name
     */
    String getGameTheme();

    /**
     * Get list of players.
     * @return List of players.
     */
    List<IPlayer> getPlayers();

    /**
     * Join player to game.
     * @param password
     */
    void join(String password);

}
