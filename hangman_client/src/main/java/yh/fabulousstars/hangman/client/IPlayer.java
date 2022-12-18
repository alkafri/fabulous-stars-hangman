package yh.fabulousstars.hangman.client;

public interface IPlayer {

    /**
     * Get client id for this player.
     * @return Client id
     */
    String getClientId();

    /**
     * Get game instance this player belongs to.
     * @return game instance
     */
    IGame getGame();

    /**
     * Get player name.
     * @return Name
     */
    String getName();

    /**
     * Get player state.
     * @return PlayetState
     */
    PlayState getPlayState();

    /**
     * Submit player word.
     * @param value
     */
    void submitWord(String value);

    /**
     * Submit player guess.
     * @param value
     */
    void submitGuess(String value);
}
