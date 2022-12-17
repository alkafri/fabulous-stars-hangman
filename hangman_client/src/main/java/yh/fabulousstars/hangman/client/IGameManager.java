package yh.fabulousstars.hangman.client;

public interface IGameManager {

    /**
     * Request list of games.
     * Generates a GameList event from server.
     */
    void listGames();

    /**
     * Join player to game.
     * @param password
     */
    void join(String gameId, String password);

    /**
     * Get game interface for current game.
     * @return IGame
     */
    IGame getGame();

    /**
     * Request creation of a new game.
     * Generates a GameCreated or CreateFailed from server.
     * @param name Game name
     * @param password Password
     */
    void createGame(String name, String password);

    /**
     * Get this player interface.
     * @return IPlayer
     */
    IPlayer getClient();

    /**
     * Connect to server.
     * @param name player name
     */
    void connect(String name);

    /**
     * Graceful disconnect from server.
     */
    void disconnect();

    /**
     * Graceful shutdown of this manager.
     */
    void shutdown();

}
