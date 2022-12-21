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
import yh.fabulousstars.hangman.client.IPlayer;
import yh.fabulousstars.hangman.client.events.*;
import yh.fabulousstars.hangman.game.GameInfo;

import java.net.URL;
import java.util.ResourceBundle;

public class LobbyController implements Initializable {

    private final IGameManager gameManager;
    private final ObservableList<GameInfo> gameList;
    private final ObservableList<IPlayer> playerList;
    private final ObservableList<String> chatList;
    @FXML
    public ListView<String> chatListView;
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
    public TextField chatInput;
    @FXML
    public ListView<GameInfo> gameListView;
    @FXML
    public ListView<IPlayer> playerListView;
    @FXML
    public Button joinButton;
    private GameStage gameWindow;

    /**
     * Constructor
     */
    public LobbyController() {
        this.gameManager = GameManagerFactory.create(this::handleGameEvent);
        this.gameWindow = null;
        this.playerList = FXCollections.observableArrayList();
        this.gameList = FXCollections.observableArrayList();
        this.chatList = FXCollections.observableArrayList();
    }

    /**
     * Create game clicked.
     *
     * @param event
     */
    @FXML
    public void onCreateButtonClick(ActionEvent event) {

        var name = gameNameField.getText().strip();
        var password = joinPasswordField.getText();
        if (!name.isEmpty()) {
            setUIState(false, UISection.Create, UISection.Join);
            gameManager.createGame(name, password);
        } else {
            //Show error
            DialogHelper.showMessage("Game & Player Name is required", Alert.AlertType.ERROR);
        }
    }

    public void onConnectButton(ActionEvent actionEvent) {
        var playerName = playerNameField.getText().strip();
        if (!playerName.isEmpty()) {
            gameManager.connect(playerName);
        }
    }

    /**
     * Enable of disable UI section.
     *
     * @param enabled  Boolean
     * @param sections Sections
     */
    private void setUIState(boolean enabled, UISection... sections) {
        for (var section : sections) {
            if (section.equals(UISection.Connect)) {
                connectButton.setDisable(!enabled);
                playerNameField.setDisable(!enabled);
            } else if (section.equals(UISection.Create)) {
                gameNameField.setDisable(!enabled);
                joinPasswordField.setDisable(!enabled);
                createButton.setDisable(!enabled);
            } else if (section.equals(UISection.Join)) {
                gameListView.setDisable(!enabled);
                joinButton.setDisable(!enabled);
            } else if (section.equals(UISection.Chat)) {
                chatInput.setDisable(!enabled);
                chatListView.setDisable(!enabled);
            }
        }
    }

    @FXML
    public void onJoinButtonClick(ActionEvent event) {
        var gameRef = gameListView.getSelectionModel().getSelectedItem();
        String password = null;
        if (gameRef.hasProtection()) {
            password = DialogHelper.promptString("Insert password.");
            if (password == null) return;
        }
        if (DialogHelper.showMessage("Join game '" + gameRef.getName() + "'?", Alert.AlertType.CONFIRMATION)) {
            setUIState(false, UISection.Join, UISection.Connect, UISection.Create, UISection.Chat);
            gameManager.join(gameRef.getGameId(), password);
        }
    }

    /**
     * Initialize game controller.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        GameApplication.getAppStage().setOnHiding(windowEvent -> {
            gameManager.disconnect();
            gameManager.shutdown();
        });

        gameListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<GameInfo> call(ListView<GameInfo> gameListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(GameInfo game, boolean b) {
                        super.updateItem(game, b);
                        if (game == null) {
                            setText("");
                        } else {
                            setText(game.getName());
                        }
                    }
                };
            }
        });
        gameListView.setItems(gameList);

        // for showing name in listvbiew
        playerListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<IPlayer> call(ListView<IPlayer> playerListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(IPlayer player, boolean b) {
                        super.updateItem(player, b);
                        if (player == null) {
                            setText("");
                        } else {
                            setText(player.getName());
                        }
                    }
                };
            }
        });
        playerListView.setItems(playerList);

        chatListView.setItems(chatList);
        chatInput.setOnAction(this::onChatInput);

        //Keeps the canvas size updated

        setUIState(false, UISection.Join, UISection.Create, UISection.Chat);
        setUIState(true, UISection.Connect);

        GameApplication.getAppStage().setOnCloseRequest(windowEvent -> {
            gameManager.shutdown();
        });
    }

    private void onChatInput(ActionEvent actionEvent) {
        var message = chatInput.getText().trim();
        chatInput.clear();
        if (!message.isEmpty()) {
            gameManager.say(message);
        }
    }

    private void handleGameEvent(IGameEvent event) {

        var client = gameManager.getClient();
        System.out.println("LOBBY(" + (client != null ? client.getClientId() : null) + "): " + event.getType());

        if (event instanceof JoinOrCreate) {

            var evt = (JoinOrCreate) event;
            if (evt.getError() != null) {
                DialogHelper.showMessage(evt.getError(), Alert.AlertType.ERROR);
                setUIState(true, UISection.Create, UISection.Chat);
                setUIState(!gameList.isEmpty(), UISection.Join);
            } else {
                setUIState(false, UISection.Connect, UISection.Join, UISection.Create, UISection.Chat);
                gameWindow = new GameStage(evt.getGame());
            }

        } else if (event instanceof PlayerState) {

            gameWindow.handlePlayerState((PlayerState) event);

        } else if (event instanceof LeaveGame) {

            if(gameWindow!=null) {
                gameWindow.close();
                gameWindow = null;
            }
            setUIState(true, UISection.Create, UISection.Chat);
            setUIState(!gameList.isEmpty(), UISection.Join);

        } else if (event instanceof GameStarted) {

            gameWindow.handleGameStarted((GameStarted) event);

        } else if (event instanceof RequestWord) {

            gameWindow.handleRequestWord((RequestWord) event);

        } else if (event instanceof RequestGuess) {

            gameWindow.handleRequestGuess((RequestGuess) event);

        } else if (event instanceof GuessResult) {

            gameWindow.handleGuessResult((GuessResult) event);

        } else if (event instanceof GameList) {

            var evt = (GameList) event;
            gameList.clear();
            gameList.addAll(evt.getGameList());
            var canJoin = !gameList.isEmpty() && gameManager.getClient().getGame() == null;
            setUIState(canJoin, UISection.Join);

        } else if (event instanceof PlayerList) {

            var evt = (PlayerList) event;

            if (evt.isInGame()) {
                gameWindow.handlePlayerList(gameManager.getGame());
            } else {
                playerList.clear();
                playerList.addAll(evt.getPlayerList());
            }

        } else if (event instanceof ClientConnect) {

            var evt = (ClientConnect) event;
            var err = evt.getError();
            if (err != null) {
                DialogHelper.showMessage(err, Alert.AlertType.ERROR);
                setUIState(true, UISection.Connect);
            } else {
                setUIState(false, UISection.Connect);
                setUIState(true, UISection.Create, UISection.Chat);
                if (!gameList.isEmpty()) {
                    setUIState(true, UISection.Join);
                }
            }

        } else if (event instanceof ChatMessage) {

            var evt = (ChatMessage) event;
            if (evt.isInGame()) {
                gameWindow.handleChatMessage((ChatMessage) event);
            } else {
                chatList.add(0, evt.getMessage());
            }

        } else if (event instanceof ResetClient) {

            setUIState(true, UISection.Connect);
            setUIState(false, UISection.Join, UISection.Create);
            gameManager.disconnect();
            chatList.clear();
            if(gameWindow!=null) {
                gameWindow.close();
                gameWindow = null;
            }
        } else if (event instanceof GameOver) {
            gameWindow.handleGameOver((GameOver) event);
        }
    }

    enum UISection {
        Connect,
        Create,
        Join,
        Chat
    }
}
