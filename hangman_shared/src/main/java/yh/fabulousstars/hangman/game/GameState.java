package yh.fabulousstars.hangman.game;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The game state holds the actual state of the game.
 * GameLogics methods operate on GameState instances.
 */
public class GameState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1020304051L;
    private final HashMap<String, String> wordBucket;
    private final HashMap<String, PlayState> players;
    private boolean ended;

    public GameState() {
        this.wordBucket = new HashMap<>();
        this.players = new HashMap<>();
        this.ended = false;
    }

    void setPlayerWord(String clientId, String word) {
        wordBucket.put(clientId, word.toUpperCase());
    }

    public HashMap<String, String> getWordBucket() {
        return wordBucket;
    }

    public List<Map.Entry<String,PlayState>> getPlayerEntries() {
        return players.entrySet().stream().toList();
    }
    public List<PlayState> getPlayerStates() {
        return players.values().stream().toList();
    }
    public List<PlayState> getLivingPlayerStates() {
        var states = new ArrayList<PlayState>();
        for(var player : players.values()) {
            if(player.getPlayState() != PlayState.DEAD) {
                states.add(player);
            }
        }
        return states;
    }

    public PlayState getPlayState(String clientId) {
        return players.get(clientId);
    }

    public void addPlayer(String clientId) {
        players.put(clientId, new PlayState(clientId));
    }

    public void removePlayer(String clientId) {
        players.remove(clientId);
    }

    public boolean getEnded() {
        return ended;
    }

    public void setEnded() {
        ended = true;
    }
}
