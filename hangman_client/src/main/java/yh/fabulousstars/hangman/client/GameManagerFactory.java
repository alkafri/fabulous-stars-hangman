package yh.fabulousstars.hangman.client;

public final class GameManagerFactory {
    //private static final String BACKEND_URL = "http://localhost:8080";
    private static final String BACKEND_URL = "https://yh-projects.ew.r.appspot.com";

    public static IGameManager create(IGameEventHandler handler) {
        return new GameManager(BACKEND_URL, handler);
    }
}
