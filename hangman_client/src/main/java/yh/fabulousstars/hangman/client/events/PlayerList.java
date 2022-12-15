package yh.fabulousstars.hangman.client.events;

import java.util.List;

public class PlayerList extends AbstractEvent {
    public record Player(String clientId, String name) {}
    private List<PlayerList.Player> playerList;

    public PlayerList(List<PlayerList.Player> playerList) {
        this.playerList = playerList;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }
}
