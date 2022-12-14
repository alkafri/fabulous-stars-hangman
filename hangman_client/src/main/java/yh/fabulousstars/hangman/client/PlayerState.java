package yh.fabulousstars.hangman.client;

public enum PlayerState {
    WaitForStart,
    SubmitWord,
    WaitForTurn,
    SubmitGuess,
    Dead,
    Winner
}
