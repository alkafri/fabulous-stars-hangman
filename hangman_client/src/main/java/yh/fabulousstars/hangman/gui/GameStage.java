package yh.fabulousstars.hangman.gui;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import yh.fabulousstars.hangman.GameApplication;
import yh.fabulousstars.hangman.MediaHelper;
import yh.fabulousstars.hangman.client.IGame;
import yh.fabulousstars.hangman.client.IPlayer;
import yh.fabulousstars.hangman.client.events.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    public void handleSubmitGuess(SubmitGuess event) {
        controller.handleSubmitGuess(event);
    }

    public void handlePlayerList(PlayerList event) {
        controller.handlePlayerList(event);
    }

    public void handleChatMessage(ChatMessage event) {
        controller.handleChatMessage(event);
    }
}

