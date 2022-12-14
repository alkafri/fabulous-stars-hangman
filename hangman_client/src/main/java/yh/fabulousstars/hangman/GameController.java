package yh.fabulousstars.hangman;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import yh.fabulousstars.hangman.client.IGame;
import yh.fabulousstars.hangman.client.IGameEvent;
import yh.fabulousstars.hangman.client.events.GameStarted;
import yh.fabulousstars.hangman.client.events.PlayerDamage;
import yh.fabulousstars.hangman.client.events.PlayerJoined;
import yh.fabulousstars.hangman.client.events.SubmitWord;
import yh.fabulousstars.hangman.client.GameClient;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    private static final String BACKEND_URL = "http://localhost:8080";

    enum UISection {
        Create,
        Join
    }

    @FXML
    public TextArea logTextArea;
    @FXML
    public Button createButton;
    @FXML
    public TextField gameNameField;
    @FXML
    public TextField playerNameField;
    @FXML
    public TextField joinPasswordField;
    @FXML
    public ListView<IGame> gameListView;
    @FXML
    public Button joinButton;
    private GameClient gameClient;
    private ObservableList<IGame> gameList;

    /**
     * Create game clicked.
     * @param event
     */
    @FXML
    public void onCreateButtonClick(ActionEvent event) {

        var name = gameNameField.getText().strip();
        var playerName = playerNameField.getText().strip();
        var password = joinPasswordField.getText();
        if(!(name.isEmpty() || playerName.isEmpty())) {
            setUIState(false, UISection.Create, UISection.Join);
            gameClient.createGame(name, playerName, password);
        } else {
            //TODO: Show error
        }
    }

    /**
     * Enable of disable UI section.
     * @param enabled Boolean
     * @param sections Sections
     */
    private void setUIState(boolean enabled, UISection... sections) {
        for(var section : sections) {
            if(section.equals(UISection.Create)) {
                gameNameField.setDisable(!enabled);
                playerNameField.setDisable(!enabled);
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

        setUIState(false, UISection.Join);
        gameClient = new GameClient(BACKEND_URL, this::handleGameEvent);
    }

    private void handleGameEvent(IGameEvent event) {
        if(event instanceof PlayerJoined) {
            //...
        } else if (event instanceof PlayerDamage) {

        } else if (event instanceof GameStarted) {

        } else if (event instanceof SubmitWord) {
            // TODO: Submit word
        }
    }
}