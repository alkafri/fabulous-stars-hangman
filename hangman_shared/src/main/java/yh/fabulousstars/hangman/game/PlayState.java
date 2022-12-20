package yh.fabulousstars.hangman.game;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Player play state.
 */
public class PlayState implements Serializable {
    public static final int WAIT = 0;
    public static final int DEAD = 1;
    public static final int FINISHED = 2;
    @Serial
    private static final long serialVersionUID = 1020304052L;
    private String clientId;
    private String opponentId;
    private String currentWord;
    private ArrayList<Character> wrongGuesses;
    private ArrayList<Character> correctGuesses;
    private int state;
    private int damage;

    public PlayState(String clientId) {
        this.clientId = clientId;
        this.opponentId = null;
        this.currentWord = null;
        this.wrongGuesses = new ArrayList<>();
        this.correctGuesses = new ArrayList<>();
        this.state = WAIT;
        this.damage = 0;
    }

    /**
     * Resets guessing and sets new word.
     *
     * @param currentWord
     */
    void setCurrentWord(String opponentId, String currentWord) {
        this.opponentId = opponentId;
        this.currentWord = currentWord;
        damage = getTotalDamage(); // accumulate damage from previous round
        wrongGuesses.clear();
        for (int i = 0; i < currentWord.length(); i++) {
            correctGuesses.add('*');
        }
        correctGuesses.clear();
    }

    public String getClientId() {
        return clientId;
    }

    String getCurrentWord() {
        return currentWord;
    }

    ArrayList<Character> getWrongGuesses() {
        return wrongGuesses;
    }

    ArrayList<Character> getCorrectGuesses() {
        return correctGuesses;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public int getPlayState() {
        return state;
    }

    int getTotalDamage() {
        return wrongGuesses.size() + damage;
    }

    public void setPlayerState(int state) {
        this.state = state;
    }

    public void addDamage() {
        this.damage++;
    }
}
