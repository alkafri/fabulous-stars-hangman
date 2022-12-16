package yh.fabulousstars.server.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameState {
    private HashMap<String,String> wordBucket;
    private List<String> players;

    public GameState() {
        this.wordBucket = new HashMap<>();
        this.players = new ArrayList<>();
    }

    public void setWord(String clientId, String word) {
        wordBucket.put(clientId, word);
    }

    public String getWord(String clientId) {
        return wordBucket.get(clientId);
    }

    public void removeWord(String clientId) {
        wordBucket.remove(clientId);
    }

    public List<String> getPlayers() {
        return players;
    }
}
