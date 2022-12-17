package yh.fabulousstars.hangman;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import yh.fabulousstars.hangman.client.GameManagerFactory;
import yh.fabulousstars.hangman.client.IGameEvent;
import yh.fabulousstars.hangman.client.IGameManager;
import yh.fabulousstars.hangman.client.events.*;
import yh.fabulousstars.hangman.client.GameClient;
import yh.fabulousstars.hangman.gui.GameStage;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {
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
    private final IGameManager gameManager;
    private final ObservableList<GameList.Game> gameList;
    private final ObservableList<PlayerList.Player> playerList;
    private final ObservableList<String> chatList;
    private GameStage gameWindow;

    /**
     * Constructor
     */
    public GameController() {
        this.gameManager = GameManagerFactory.create(this::handleGameEvent);
        this.gameWindow = null;
        this.playerList = FXCollections.observableArrayList();
        this.gameList = FXCollections.observableArrayList();
        this.chatList = FXCollections.observableArrayList();
    }
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
            gameManager.createGame(name, password);
        } else {
            //Show error
            showMessage("Game & Player Name is required", Alert.AlertType.ERROR);
        }
    }

    public void onConnectButton(ActionEvent actionEvent) {
        var playerName = playerNameField.getText().strip();
        if(!playerName.isEmpty()) {
            gameManager.connect(playerName);
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
        var gameRef = gameListView.getSelectionModel().getSelectedItem();
        String password = null;
        if(gameRef.hasPassword()) {
            password = prompt
        }
        if(showMessage("Join game '"+gameRef.name()+"'?", Alert.AlertType.CONFIRMATION)) {
            setUIState(false, UISection.Join, UISection.Connect, UISection.Create);
            gameManager.join(gameRef.gameId(), password);
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
        lobbyChat.setItems(chatList);

        System.out.println("Initialized");
        //Keeps the canvas size updated

        setUIState(false, UISection.Join, UISection.Create);
        setUIState(true, UISection.Connect);

        GameApplication.getAppStage().setOnCloseRequest(windowEvent -> {
            gameManager.shutdown();
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
                gameWindow = new GameStage(evt.getGame());
            }
        } else if (event instanceof PlayerJoined) {
            gameWindow.handlePlayerJoined((PlayerJoined)event);
        } else if (event instanceof PlayerLeft) {
            gameWindow.handlePlayerLeft((PlayerLeft)event);
        } else if (event instanceof PlayerState) {
            gameWindow.handlePlayerState((PlayerState)event);
        } else if (event instanceof GameCreate) {
            var evt = (GameCreate)event;
            if(evt.getError()==null) {
                showMessage(evt.getError(), Alert.AlertType.ERROR);
                setUIState(true, UISection.Join, UISection.Create);
            } else {
                setUIState(false, UISection.Connect, UISection.Join, UISection.Create);
                gameWindow = new GameStage(evt.getGame());
            }
        } else if (event instanceof LeaveGame) {
            gameWindow.close();
            gameWindow = null;
            setUIState(true, UISection.Join, UISection.Create);
        } else if (event instanceof GameStarted) {
            gameWindow.handleGameStarted((GameStarted)event);
        } else if (event instanceof SubmitWord) {
            gameWindow.handleSubmitWord((SubmitWord)event);
        } else if (event instanceof SubmitGuess) {
            gameWindow.handleSubmitGuess((SubmitGuess)event);
        } else if (event instanceof GameList) {
            var evt = (GameList)event;
            gameList.clear();
            gameList.addAll(evt.getGameList());
        } else if (event instanceof PlayerList) {
            var evt = (PlayerList)event;
            if(evt.isInGame()) {
                gameWindow.handlePlayerList((PlayerList)event);
            } else {
                playerList.clear();
                playerList.addAll(evt.getPlayerList());
            }
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
        } else if (event instanceof ChatMessage) {
            var evt = (ChatMessage)event;
            if(evt.isInGame()) {
                gameWindow.handleChatMessage((ChatMessage)event);
            } else {
                chatList.add(0, evt.getMessage());
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
    public static String showMessage(String prompt) {
        Alert alertWindow = new Alert(type);
        alertWindow.setTitle(type.toString());
        alertWindow.setContentText(message);
        var res = alertWindow.showAndWait();
        return res.get().equals(ButtonType.OK);
    }
}
