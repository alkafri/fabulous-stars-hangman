package yh.fabulousstars.server.game;

import java.util.ArrayList;

public class PlayerState {
    private String currentWord;
    private ArrayList<Character> wrongGuesses;
    private ArrayList<Character> correctGuesses;

    public PlayerState() {
        this.currentWord = null;
        this.wrongGuesses = new ArrayList<>();
        this.correctGuesses = new ArrayList<>();
    }

    /**
     * Resets guessing and sets new word.
     *
     * @param currentWord
     */
    void setCurrentWord(String currentWord) {
        this.currentWord = currentWord;
        wrongGuesses.clear();
        correctGuesses.clear();
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
}
