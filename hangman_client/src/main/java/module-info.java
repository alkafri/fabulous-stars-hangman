module yh.fabulousstars.hangman {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.net.http;
    requires  com.google.gson;

    opens yh.fabulousstars.hangman to javafx.fxml;
    exports yh.fabulousstars.hangman;
}