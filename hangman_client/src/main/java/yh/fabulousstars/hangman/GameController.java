package yh.fabulousstars.hangman;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;
import yh.fabulousstars.hangman.client.IGame;
import yh.fabulousstars.hangman.client.IGameEvent;
import yh.fabulousstars.hangman.client.events.*;
import yh.fabulousstars.hangman.client.GameClient;
import yh.fabulousstars.hangman.gui.CanvasClass;
import yh.fabulousstars.hangman.gui.GameStage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class GameController implements Initializable {

    private static final String BACKEND_URL = "http://localhost:8080";

    enum UISection {
        Connect,
        Create,
        Join
    }
    @FXML
    public ListView<String> lobbyChat;
    @FXML
    public Button connectButton;
    @FXML
    public Button createButton;
    @FXML
    public TextField gameNameField;
    @FXML
    public TextField playerNameField;
    @FXML
    public TextField joinPasswordField;
    @FXML
    public ListView<GameList.Game> gameListView;
    @FXML
    public ListView<PlayerList.Player> playerListView;
    @FXML
    public Button joinButton;
    private GameClient gameClient;
    private ObservableList<GameList.Game> gameList;
    private ObservableList<PlayerList.Player> playerList;
    private ObservableList<String> chatList;

    /**
     * Create game clicked.
     * @param event
     */
    @FXML
    public void onCreateButtonClick(ActionEvent event) {

        var name = gameNameField.getText().strip();
        var password = joinPasswordField.getText();
        if(!name.isEmpty()) {
            setUIState(false, UISection.Create, UISection.Join);
            gameClient.createGame(name, password);
        } else {
            //Show error
            showMessage("Game & Player Name is required", Alert.AlertType.ERROR);
        }
    }

    public void onConnectButton(ActionEvent actionEvent) {
        var playerName = playerNameField.getText().strip();
        if(!playerName.isEmpty()) {
            gameClient.connect(playerName);
        }
    }


    /**
     * Enable of disable UI section.
     * @param enabled Boolean
     * @param sections Sections
     */
    private void setUIState(boolean enabled, UISection... sections) {
        for(var section : sections) {
            if (section.equals(UISection.Connect)) {
                connectButton.setDisable(!enabled);
                playerNameField.setDisable(!enabled);
            } else if(section.equals(UISection.Create)) {
                gameNameField.setDisable(!enabled);
                joinPasswordField.setDisable(!enabled);
                createButton.setDisable(!enabled);
            } else if (section.equals(UISection.Join)) {
                gameListView.setDisable(!enabled);
                joinButton.setDisable(!enabled);
            }
        }
    }

    @FXML
    public void onJoinButtonClick(ActionEvent event) {
        var game = gameListView.getSelectionModel().getSelectedItem();
        if(showMessage("Are you sure you want to join "+game+"!!!",Alert.AlertType.CONFIRMATION)) {
            setUIState(false, UISection.Join, UISection.Connect, UISection.Create);
            // TODO: now just for show
            new GameStage(gameClient.getDummyGame());
        }
    }

    /**
     * Initialize game controller.
     * @param location
     * The location used to resolve relative paths for the root object, or
     * {@code null} if the location is not known.
     *
     * @param resources
     * The resources used to localize the root object, or {@code null} if
     * the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        gameList = FXCollections.observableArrayList();
        gameListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<GameList.Game> call(ListView<GameList.Game> gameListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(GameList.Game game, boolean b) {
                        super.updateItem(game, b);
                        if(game==null) {setText(""); }
                        else { setText(game.name()); }
                    }
                };
            }
        });
        gameListView.setItems(gameList);

        playerList = FXCollections.observableArrayList();
        playerListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<PlayerList.Player> call(ListView<PlayerList.Player> playerListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(PlayerList.Player player, boolean b) {
                        super.updateItem(player, b);
                        if(player==null) { setText(""); }
                        else { setText(player.name()); }
                    }
                };
            }
        });
        playerListView.setItems(playerList);

        chatList = FXCollections.observableArrayList();
        lobbyChat.setItems(chatList);

        System.out.println("Initialized");
        //Keeps the canvas size updated

        setUIState(false, UISection.Join, UISection.Create);
        setUIState(true, UISection.Connect);
        gameClient = new GameClient(BACKEND_URL, this::handleGameEvent);
        GameApplication.getAppStage().setOnCloseRequest(windowEvent -> {
            gameClient.shutdown();
        });
    }

    private void handleGameEvent(IGameEvent event) {
        if(event instanceof JoinGame) {
            var evt = (JoinGame)event;
            if(evt.getError()==null) {
                showMessage(evt.getError(), Alert.AlertType.ERROR);
                setUIState(true, UISection.Join, UISection.Create);
            } else {
                setUIState(false, UISection.Connect, UISection.Join, UISection.Create);
                new GameStage(evt.getGame());
            }
        } else if (event instanceof PlayerDamage) {

        } else if (event instanceof GameStarted) {

        } else if (event instanceof SubmitWord) {
        } else if (event instanceof GameList) {
            var evt = (GameList)event;
            gameList.clear();
            gameList.addAll(evt.getGameList());
        } else if (event instanceof PlayerList) {
            var evt = (PlayerList)event;
            playerList.clear();
            playerList.addAll(evt.getPlayerList());
        } else if (event instanceof ClientConnect) {
            var evt = (ClientConnect)event;
            var err = evt.getError();
            if(err != null) {
                showMessage(err, Alert.AlertType.ERROR);
                setUIState(true, UISection.Connect);
            } else {
                setUIState(false, UISection.Connect);
                setUIState(true, UISection.Join, UISection.Create);
            }
        }
    }
     public static boolean showMessage(String message, Alert.AlertType type) {
         Alert alertWindow = new Alert(type);
         alertWindow.setTitle(type.toString());
         alertWindow.setContentText(message);
         var res = alertWindow.showAndWait();
         return res.get().equals(ButtonType.OK);
    }
}
