package yh.fabulousstars.hangman;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import yh.fabulousstars.hangman.client.IGame;
import yh.fabulousstars.hangman.client.events.*;

import java.io.IOException;

public class GameStage extends Stage {
    private GameController controller;

    public GameStage(IGame game) {
        FXMLLoader fxmlLoader = new FXMLLoader(GameApplication.class.getResource("/game-view.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), Color.GRAY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.controller = fxmlLoader.getController();
        this.controller.setGame(game);
        setOnCloseRequest(this::handleClose);
        setTitle("Hangman: " + game.getName());
        initOwner(GameApplication.getAppStage());
        setScene(scene);
        setResizable(false);
        show();
    }

    private void handleClose(WindowEvent windowEvent) {
        windowEvent.consume();
        controller.dispose();
        close();
    }

    public void handlePlayerState(PlayerState event) {
        controller.handlePlayerState(event);
    }

    public void handleGameStarted(GameStarted event) {
        controller.handleGameStarted(event);
    }

    public void handleRequestWord(RequestWord event) {
        controller.handleRequestWord(event);
    }

    public void handleRequestGuess(RequestGuess event) {
        controller.handleRequestGuess(event);
    }

    public void handleGuessResult(GuessResult event) {
        controller.handleGuessResult(event);
    }

    public void handlePlayerList(IGame game) {
        controller.handlePlayerList(game);
    }

    public void handleChatMessage(ChatMessage event) {
        controller.handleChatMessage(event);
    }

    public void handleGameOver(GameOver event) {
        controller.handleGameOver(event);
    }
}

