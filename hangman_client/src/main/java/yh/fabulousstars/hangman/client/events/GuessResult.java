package yh.fabulousstars.hangman.client.events;

public class GuessResult extends AbstractEvent {
    private boolean correct;
    private boolean finished;
    private String guess;

    public GuessResult(boolean correct, boolean finished, String guess) {
        this.correct = correct;
        this.finished = finished;
        this.guess = guess;
    }

    public String getGuess() {
        return guess;
    }

    public boolean isCorrect() {
        return correct;
    }
    public boolean isFinished() {
        return finished;
    }
}
