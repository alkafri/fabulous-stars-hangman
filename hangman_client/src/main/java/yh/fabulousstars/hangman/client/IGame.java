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

    void leave();
}
