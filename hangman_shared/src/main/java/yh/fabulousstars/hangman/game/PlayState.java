package yh.fabulousstars.hangman.game;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Player play state.
 */
public class PlayState implements Serializable {
    public static final int PLAY = 0;
    public static final int DEAD = 1;
    public static final int FINISHED = 2;
    @Serial
    private static final long serialVersionUID = 1020304052L;
    private final String clientId;
    private String opponentId;
    private char[] currentWord;
    private final ArrayList<Character> wrongGuesses;
    private char[] correctGuesses;
    private int state;
    private int damage;

    public PlayState(String clientId) {
        this.clientId = clientId;
        this.opponentId = null;
        this.currentWord = null;
        this.wrongGuesses = new ArrayList<>();
        this.correctGuesses = null;
        this.state = PLAY;
        this.damage = 0;
    }

    /**
     * Resets guessing and sets new word.
     *
     * @param currentWord
     */
    void setCurrentWord(String opponentId, String currentWord) {
        this.opponentId = opponentId;
        this.currentWord = currentWord.toUpperCase().toCharArray();
        wrongGuesses.clear();
        correctGuesses = new char[currentWord.length()];
        for (int i = 0; i < currentWord.length(); i++) {
            correctGuesses[i] = '*';
        }
    }

    public String getClientId() {
        return clientId;
    }

    public char[] getCurrentWord() {
        return currentWord;
    }

    public ArrayList<Character> getWrongGuesses() {
        return wrongGuesses;
    }

    public char[] getCorrectGuesses() {
        return correctGuesses;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public int getPlayState() {
        return state;
    }

    public int getDamage() {
        return damage;
    }

    public void setPlayerState(int state) {
        this.state = state;
    }

    public void addDamage() {
        if (++damage >= Config.MAX_DAMAGE) {
            state = PlayState.DEAD;
        }
    }
}
