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
import yh.fabulousstars.hangman.client.GameClient;
import yh.fabulousstars.hangman.gui.CanvasClass;
import java.util.ResourceBundle;
import java.util.Scanner;

public class GameController implements Initializable {

    private static final String BACKEND_URL = "http://localhost:8080";
    int guesses = 0;
    int correctGuess = 0;


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


        //Prints the black bar
        blackBarForLetter();
        //Draws the hangman
        hangmanFigure();
        //draws the wrongly guessed letters
        addWrongLetter();
        //draws the correctly guessed word
        addCorrectLetter();
    }
    public void blackBarForLetter() {
        //Temporary until the proper word count can be used
        int wordCount = 7;
        int maxBarSize = 60;
        int barWidth = (int) (canvas.getWidth()*0.01);
        int barHeight = (int) (canvas.getHeight()*0.02);
        int barSize = barWidth*barHeight;

        if (barSize > maxBarSize) {
            barSize = maxBarSize;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();

        //Prints the image same amount of times as a word has letters
        for (int i = 0; wordCount > i; i++) {

        Image image = new Image("BlackBarTR.png");
        gc.drawImage(image,barSize*i*1.5, canvas.getHeight()*0.8,barSize,canvas.getHeight()*0.01);
        }
    }
    public void hangmanFigure() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // i = the amount of wrong guesses
        int i = guesses;

        /*NOTE to self
        *Make own images
        * 1 for each state the hangman can be in
        * make them with transparent background
        * experiment with what a good size is
        * */

        if (i == 1) {
            Image image = new Image("HangmanTranState1.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5 );
        }
        if (i == 2) {
            Image image = new Image("HangmanTranState2.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }
        if (i == 3) {
            Image image = new Image("HangmanTranState3.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }
        if (i == 4) {
            Image image = new Image("HangmanTranState4.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }
        if (i == 5) {
            Image image = new Image("HangmanTranState5.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }
        if (i == 6) {
            Image image = new Image("HangmanTranState6.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }
        if (i == 7) {
            Image image = new Image("HangmanTranState7.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }
        if (i == 8) {
            Image image = new Image("HangmanTranState8.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }
        if (i == 9) {
            Image image = new Image("HangmanTranState9.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }
        if (i == 10) {
            Image image = new Image("HangmanTranState10.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }
        if (i >= 11) {
            Image image = new Image("HangmanTranState11.png");
            gc.drawImage(image, 0, 0, canvas.getWidth()*0.3,canvas.getHeight()*0.5);
        }

    }

    public void addWrongLetter() {
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
        Scanner scanner = new Scanner(System.in);
        int counter = -1;
        int maxLetterSize = 80;
        int letterSize = (int) (canvas.getWidth()*0.01* canvas.getHeight()*0.02);

        if (letterSize > maxLetterSize) {
            letterSize = maxLetterSize;
        }

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
                gc.fillText("B", 0+letterSpacing+ canvas.getWidth()*0.3, rowTwo);
            }
            if (i < 16 && i > 10) {
                gc.fillText("C", 0+letterSpacing+ canvas.getWidth()*0.3, rowThree);
            }
            if (i < 21 && i > 15) {
                gc.fillText("D", 0+letterSpacing+ canvas.getWidth()*0.3, rowFour);
            }

            counter++;
            if (counter > 4) {
                counter = 0;
            }
        }
    }
    public void addCorrectLetter() {
        //Temporary until the proper word count can be used
        int wordCount = 7;
        int maxBarSize = 60;
        int barWidth = (int) (canvas.getWidth()*0.01);
        int barHeight = (int) (canvas.getHeight()*0.02);
        int barSize = barWidth*barHeight;

        if (barSize > maxBarSize) {
            barSize = maxBarSize;
        }
        int maxLetterSize = 80;
        int letterSize = (int) (canvas.getWidth()*0.01* canvas.getHeight()*0.02);

        if (letterSize > maxLetterSize) {
            letterSize = maxLetterSize;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.GREEN);
        gc.setFont(new Font("Arial", letterSize));

        //Prints the image same amount of times as a word has letters
        for (int i = 0; correctGuess > i; i++) {

            if (correctGuess <= wordCount ) {
                gc.fillText("E",barSize*i*1.5, canvas.getHeight()*0.8,barSize);
            }
        }
        if (correctGuess == wordCount){
            System.out.println("YOU WIN");
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
    public ListView<String> gameListView;
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

        //move this to a new function that can determine if a guess is correct or wrong
        guesses++;
        System.out.println(guesses+"Guesses button");
        correctGuess++;
        System.out.println(correctGuess+"Correct guess button");
        addWrongLetter();
        addCorrectLetter();
        hangmanFigure();
        //^^^^^^^ to be moved to a better place

        var name = gameNameField.getText().strip();
        var playerName = playerNameField.getText().strip();
        var password = joinPasswordField.getText();
        if(!(name.isEmpty() || playerName.isEmpty())) {
            //setUIState(false, UISection.Create, UISection.Join);
            //gameClient.createGame(name, playerName, password);
            
            setUIState(true, UISection.Create, UISection.Join);
            gameManager.createGame(name, playerName, password);
            gameListView.getItems().add(name+"-"+playerName);

        } else {
            //Show error
            //showMessage("Game & Player Name is required",Alert.AlertType.ERROR.toString());
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
        showMessage("Are you sure you want to join "+game+"!!!",Alert.AlertType.CONFIRMATION.toString());
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
     public static void showMessage(String message,String alert) {

        if(alert.equals(Alert.AlertType.ERROR.toString())) {

            Alert alertWindow = new Alert(Alert.AlertType.ERROR);
            alertWindow.setTitle(Alert.AlertType.ERROR.toString());
            alertWindow.setContentText(message);
            alertWindow.showAndWait();

        }
        else if(alert.equals(Alert.AlertType.CONFIRMATION.toString())) {
            Alert alertWindow = new Alert(Alert.AlertType.CONFIRMATION);
            alertWindow.setTitle(Alert.AlertType.CONFIRMATION.toString());
            alertWindow.setContentText(message);
            alertWindow.showAndWait();
        }

    }
}
