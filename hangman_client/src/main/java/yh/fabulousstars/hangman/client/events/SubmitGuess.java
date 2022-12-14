package yh.fabulousstars.hangman.client.events;

public class SubmitGuess extends  AbstractEvent {
    private boolean correct;
    private String guess;

    public SubmitGuess(boolean correct, String guess) {
        this.correct = correct;
        this.guess = guess;
    }

    public String getGuess() {
        return guess;
    }

    public boolean isCorrect() {
        return correct;
    }
}
