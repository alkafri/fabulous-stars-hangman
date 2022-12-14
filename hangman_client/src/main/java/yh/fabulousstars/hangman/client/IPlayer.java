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
    PlayerState getState();

    /**
     * Get player damage.
     * @return 0 to MAX_DAMAGE
     */
    int getDamage();

    /**
     * Perform a submit.
     * @param value
     */
    void submit(String value);
}
