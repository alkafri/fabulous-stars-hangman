package yh.fabulousstars.hangman.game;

import java.io.Serial;
import java.io.Serializable;

public class GameInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1020304054L;
    private String gameId;
    private String name;
    private boolean protection;

    public GameInfo(String gameId, String name, boolean protection) {
        this.gameId = gameId;
        this.name = name;
        this.protection = protection;
    }

    public String getGameId() {
        return gameId;
    }

    public String getName() {
        return name;
    }

    public boolean hasProtection() {
        return protection;
    }
}
