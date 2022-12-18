package yh.fabulousstars.hangman.client;

import java.util.ArrayList;

/**
 * Player play state.
 */
public class PlayState {
    public static final int WAIT_FOR_START = 0;
    public static final int SUBMIT_WORD = 1;
    public static final int WAIT_FOR_TURN = 2;
    public static final int SUBMIT_GUESS = 3;
    public static final int DEAD = 4;
    public static final int WON = 5;

    private String clientId;
    private String opponentId;
    private String currentWord;
    private ArrayList<Character> wrongGuesses;
    private ArrayList<Character> correctGuesses;
    private int state;
    private int damage;

    public PlayState() {
        this.clientId = null;
        this.currentWord = null;
        this.opponentId = null;
        this.wrongGuesses = null;
        this.correctGuesses = null;
        this.state = WAIT_FOR_START;
        this.damage = 0;
    }

    public ArrayList<Character> getWrongGuesses() {
        return wrongGuesses;
    }

    public String getOpponentId() {
        return opponentId;
    }
    public String getCurrentWord() {
        return currentWord;
    }

    public ArrayList<Character> getCorrectGuesses() {
        return correctGuesses;
    }

    public int getState() {
        return state;
    }

    public int getTotalDamage() {
        return wrongGuesses.size() + damage;
    }

    public String getClientId() {
        return clientId;
    }
}
