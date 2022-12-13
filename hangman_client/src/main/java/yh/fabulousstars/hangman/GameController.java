package yh.fabulousstars.hangman;

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
import yh.fabulousstars.hangman.client.IGame;
import yh.fabulousstars.hangman.client.IGameEvent;
import yh.fabulousstars.hangman.client.events.GameStarted;
import yh.fabulousstars.hangman.client.events.PlayerDamage;
import yh.fabulousstars.hangman.client.events.PlayerJoined;
import yh.fabulousstars.hangman.client.events.SubmitWord;
import yh.fabulousstars.hangman.gui.CanvasClass;
import yh.fabulousstars.hangman.localclient.GameManager;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {


    int guesses = 0;

    enum UISection {
        Create,
        Join
    }

    @FXML
    private Pane parentPane;
    @FXML
    private Canvas canvas;
    Scene scene;

    @FXML
    public TextArea logTextArea;
    //Canvas background
    @FXML
    public void canvasBackground() {
        //sout is used to check if the method is initialized
        //System.out.println("initialize method called");

        //gc = set the background color
        //creating a rectangle covering 100% of the canvas makes it look like a background
        //The color is able to change
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLUE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        hangmanFigure();
        addLetter();



    }
    public void hangmanFigure() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int i = 0;

        /*NOTE to self
        *Make own images
        * 1 for each state the hangman can be in
        * make them with transparent background
        * experiment with what a good size is
        * */
        if (i == 0) {
            Image image = new Image("https://d338t8kmirgyke.cloudfront.net/icons/icon_pngs/000/001/955/original/hangman.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }
    }

    public void addLetter() {
        /*
        * if the guess is wrong make the letter appear in red
        * place them to the right of the hangman
        * have a method to check if letter is correct
        * if wrong print it on the canvas
        * Need to change a little for the final product
        * fori loop is only for testing purposes
        * IMPORTANT wrongGuess() currently only supports up to 20 guesses
        * the amount can easily be changed, but I also don't think that
         */
        int counter = -1;
        int letterSize = 80;
        int rowOne = letterSize;
        int rowTwo = letterSize*2;
        int rowThree = letterSize*3;
        int rowFour = letterSize*4;
        //change the "A" to the players input
        for (int i = 0; i < guesses+1; i++) {
            int letterSpacing = counter*letterSize;

            GraphicsContext gc = canvas.getGraphicsContext2D();

            gc.setFill(Color.RED);
            gc.setFont(new Font("Arial", letterSize));

            if (i < 6 && i > 0) {
                gc.fillText("A", 0+letterSpacing+ canvas.getWidth()*0.3, rowOne);
            }
            if (i < 11 && i > 5) {
                gc.fillText("A", 0+letterSpacing+ canvas.getWidth()*0.3, rowTwo);
            }
            if (i < 16 && i > 10) {
                gc.fillText("A", 0+letterSpacing+ canvas.getWidth()*0.3, rowThree);
            }
            if (i < 21 && i > 15) {
                gc.fillText("A", 0+letterSpacing+ canvas.getWidth()*0.3, rowFour);
            }

            counter++;
            if (counter > 4) {
                counter = 0;
            }
        }
    }
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
    private GameManager gameManager;
    private ObservableList<IGame> gameList;

    /**
     * Create game clicked.
     * @param event
     */
    @FXML
    public void onCreateButtonClick(ActionEvent event) {

        //move this to a new function that can determine if a guess is correct or wrong
        guesses++;
        System.out.println(guesses+"Guesses button");
        addLetter();
        //^^^^^^^ to be moved to a better place

        var name = gameNameField.getText().strip();
        var playerName = playerNameField.getText().strip();
        var password = joinPasswordField.getText();
        if(!(name.isEmpty() || playerName.isEmpty())) {
            setUIState(false, UISection.Create, UISection.Join);
            gameManager.createGame(name, playerName, password);
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
        for(var section : sections){
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

        System.out.println("Initialized");
        //Keeps the canvas size updated
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> canvasBackground());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> canvasBackground());


        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        setUIState(false, UISection.Join);
        gameManager = new GameManager(this::handleGameEvent);
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
