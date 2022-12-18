package yh.fabulousstars.hangman.client.events;

import yh.fabulousstars.hangman.client.IPlayer;

import java.util.List;

public class PlayerList extends AbstractEvent {
    private final boolean inGame;
    private final List<IPlayer> playerList;

    public PlayerList(List<IPlayer> playerList, boolean inGame) {
        this.inGame = inGame;
        this.playerList = playerList;
    }

    public List<IPlayer> getPlayerList() {
        return playerList;
    }

    public boolean isInGame() {
        return inGame;
    }
}
