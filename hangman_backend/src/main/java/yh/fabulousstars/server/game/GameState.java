package yh.fabulousstars.server.game;

import java.util.*;

/**
 * The game state holds the actual state of the game.
 * GameLogics methods operate on GameState instances.
 */
public class GameState {
    private HashMap<String,String> wordBucket;
    private HashMap<String,PlayerState> players;

    public GameState() {
        this.wordBucket = new HashMap<>();
        this.players = new HashMap<>();
    }

    void setWord(String clientId, String word) {
        wordBucket.put(clientId, word);
    }

    /**
     * Get random word from bucket for each player not belonging to player.
     */
    void chooseWords() {
        // map of clientId -> word
        var wordKeys = new ArrayList<>(wordBucket.keySet());
        Collections.shuffle(wordKeys); // shuffle key order
        // choose word for each player
        for (var player : getPlayers()) {
            // get first word not belonging to player
            for (int i = 0; i < wordKeys.size(); i++) {
                var wordKey = wordKeys.get(i);
                if(!wordKey.equals(player.getKey())) {
                    player.getValue().setCurrentWord(wordBucket.get(wordKey));
                    wordKeys.remove(wordKey);
                    break;
                }
            }
        }
        wordBucket.keySet();
    }

    void removeWord(String clientId) {
        wordBucket.remove(clientId);
    }

    public Set<Map.Entry<String, PlayerState>> getPlayers() {
        return players.entrySet();
    }

    public void addPlayer(String clientId) {
        players.put(clientId, new PlayerState());
    }

    public void removePlayer(String clientId) {
        players.remove(clientId);
    }
}
