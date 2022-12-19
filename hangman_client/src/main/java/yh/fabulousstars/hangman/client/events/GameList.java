package yh.fabulousstars.hangman.client.events;

import yh.fabulousstars.hangman.game.GameInfo;

import java.util.List;

public class GameList extends AbstractEvent {
    private final List<GameInfo> gameList;

    public GameList(List<GameInfo> gameList) {
        this.gameList = gameList;
    }

    public List<GameInfo> getGameList() {
        return gameList;
    }
}
