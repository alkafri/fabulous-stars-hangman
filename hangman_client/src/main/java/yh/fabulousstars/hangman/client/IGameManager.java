package yh.fabulousstars.hangman.client;

import java.util.List;

public interface IGameManager {

    /**
     * Get list of games.
     * @return List of games.
     */
    void listGames();

    /**
     * Create a new game.
     * Generates a GameCreated or CreateFailed.
     * @param name Game name
     * @param playerName Player name
     * @param password Password
     */
    void createGame(String name, String playerName, String password);

    /**
     * Get this clients unique session id.
     * @return is
     */
    String getClientId();

    void connect(String password);

    void join(String gameId);
}
