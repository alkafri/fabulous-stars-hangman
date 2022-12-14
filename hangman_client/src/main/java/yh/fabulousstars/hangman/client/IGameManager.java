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
     * @param playerName Player name
     * @param password Password
     */
    void createGame(String name, String playerName, String password);

    /**
     * Get this client player.
     * @return is
     */
    IPlayer getClient();

    void connect(String name, String password);
}
