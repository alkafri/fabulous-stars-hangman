package yh.fabulousstars.server.game;

import java.util.*;

/**
 * The game state holds the actual state of the game.
 * GameLogics methods operate on GameState instances.
 */
public class GameState {
    private boolean started;
    private HashMap<String,String> wordBucket;
    private HashMap<String, PlayState> players;

    public GameState() {
        this.wordBucket = new HashMap<>();
        this.players = new HashMap<>();
        this.started = false;
    }

    void setPlayerWord(String clientId, String word) {

        wordBucket.put(clientId, word.toUpperCase());
    }

    /**
     * Get random word from bucket for each player not belonging to player.
     */
    void chooseWords() {
        // map of clientId -> word
        var opponentIds = new ArrayList<>(wordBucket.keySet());
        Collections.shuffle(opponentIds); // shuffle word order
        // choose word for each player
        for (var player : getPlayers()) {
            // get first word not belonging to player
            for (int i = 0; i < opponentIds.size(); i++) {
                var opponentId = opponentIds.get(i);
                if(!opponentId.equals(player.getClientId())) {
                    // set word
                    player.setCurrentWord(opponentId, wordBucket.get(opponentId));
                    // remove used word from bucket
                    opponentIds.remove(opponentId);
                    break;
                }
            }
        }
    }

    void removeWord(String clientId) {
        wordBucket.remove(clientId);
    }

    public List<PlayState> getPlayers() {
        return players.values().stream().toList();
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

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean getStarted() {
        return started;
    }

    /**
     * Bucket is filled with words.
     * @return
     */
    public boolean hasWords() {
        return players.size()==wordBucket.size();
    }
}
