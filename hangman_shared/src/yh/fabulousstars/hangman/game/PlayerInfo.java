package yh.fabulousstars.hangman.game;

import java.io.Serial;
import java.io.Serializable;

public class PlayerInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1020304053L;

    private String name;
    private String clientId;
    private String gameId;

    public PlayerInfo(String name, String clientId, String gameId) {
        this.name = name;
        this.clientId = clientId;
        this.gameId = gameId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getName() {
        return name;
    }
}
