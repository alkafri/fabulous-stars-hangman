package yh.fabulousstars.hangman.client;

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
     * @param password Password
     */
    void createGame(String name, String password);

    /**
     * Get this client player.
     * @return is
     */
    IPlayer getClient();

    /**
     *
     * @param name
     */
    void connect(String name);

    void disconnect();

    /**
     *
     */
    void shutdown();

}
