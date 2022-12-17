package yh.fabulousstars.hangman.client.events;

import java.util.List;

public class PlayerList extends AbstractEvent {
    private final boolean inGame;

    public record Player(String clientId, String name) {}
    private List<PlayerList.Player> playerList;

    public PlayerList(List<PlayerList.Player> playerList, boolean inGame) {
        this.inGame = inGame;
        this.playerList = playerList;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public boolean isInGame() {
        return inGame;
    }
}
