package yh.fabulousstars.hangman.client;

public final class GameManagerFactory {
    private static final String BACKEND_URL = "http://localhost:8080";

    public static IGameManager create(IGameEventHandler handler) {
        return new GameManager(BACKEND_URL,handler);
    }
}
