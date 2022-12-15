package yh.fabulousstars.hangman.client.events;

public class ChatMessage extends AbstractEvent {
    private final String message;
    private final boolean inGame;

    public ChatMessage(String message, boolean inGame) {
        this.message = message;
        this.inGame = inGame;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Get if message is in-game, else lobby.
     *
     * @return
     */
    public boolean isInGame() {
        return inGame;
    }
}
