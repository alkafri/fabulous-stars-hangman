module yh.fabulousstars.hangman {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.net.http;

    opens yh.fabulousstars.hangman to javafx.fxml;
    exports yh.fabulousstars.hangman;
    exports yh.fabulousstars.hangman.gui;
}