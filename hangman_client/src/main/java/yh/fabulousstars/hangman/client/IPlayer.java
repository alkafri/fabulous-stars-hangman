package yh.fabulousstars.hangman.client;

import yh.fabulousstars.hangman.game.PlayState;

public interface IPlayer {

    /**
     * Get manager the game belongs to.
     *
     * @return IGameManager instance
     */
    IGameManager getManager();

    /**
     * Get client id for this player.
     *
     * @return Client id
     */
    String getClientId();

    /**
     * Get game instance this player belongs to.
     *
     * @return game instance
     */
    IGame getGame();

    /**
     * Get player name.
     *
     * @return Name
     */
    String getName();

    /**
     * Get player state.
     *
     * @return PlayetState
     */
    PlayState getPlayState();
}
