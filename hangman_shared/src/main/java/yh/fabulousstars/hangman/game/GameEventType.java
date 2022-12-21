package yh.fabulousstars.hangman.game;

import java.io.Serializable;

public enum GameEventType implements Serializable {
    Idle,
    Winner,
    Loser,
    Connected,
    Connect_error,
    Created,
    Create_error,
    Join,
    Join_error,
    Game_list,
    Player_list,
    Leave,
    Message,
    Game_started,
    Play_state,
    Guess_result,
    Request_word,
    Request_guess,
    Reset
}
