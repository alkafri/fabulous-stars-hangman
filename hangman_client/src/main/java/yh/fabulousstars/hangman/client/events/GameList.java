package yh.fabulousstars.hangman.client.events;

import java.util.List;

public class GameList extends AbstractEvent {
    public record Game(String gameId, String name, boolean hasPassword) {}
    private List<Game> gameList;

    public GameList(List<Game> gameList) {
        this.gameList = gameList;
    }

    public List<Game> getGameList() {
        return gameList;
    }
}
