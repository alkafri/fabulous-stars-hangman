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
import yh.fabulousstars.hangman.localclient.GameManager;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Scanner;

public class GameController implements Initializable {


    public TextField guessField;
    int wrongGuesses = 0;
    int correctGuess = 0;
    String userWord = "hello".toUpperCase();
    int wordCount = userWord.length();

    char[] correctLetter = new char[wordCount];
    char[] wrongLetter = new char[12];



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
        gc.setFill(Color.LIGHTSKYBLUE);
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

        var images = Arrays.asList(
                "HangmanTranState1.png",
                "HangmanTranState2.png",
                "HangmanTranState3.png",
                "HangmanTranState4.png",
                "HangmanTranState5.png",
                "HangmanTranState6.png",
                "HangmanTranState7.png",
                "HangmanTranState8.png",
                "HangmanTranState9.png",
                "HangmanTranState10.png",
                "HangmanTranState11.png");

        for (int i = 0; i < wrongGuesses; i++) {
            Image image = new Image(images.get(i));
            gc.drawImage(image, 0, 10, canvas.getWidth()*0.3,canvas.getHeight()*0.5 );

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
        int maxLetterSize = 100;
        int letterSize = (int) (canvas.getWidth()*0.01* canvas.getHeight()*0.02);

        if (letterSize > maxLetterSize) {
            letterSize = maxLetterSize;
        }

        int rowOne = letterSize;
        int rowTwo = letterSize*2;
        //change the "A" to the players input
        for (int i = 0; i < wrongGuesses+1; i++) {
            int letterSpacing = counter*letterSize;

            GraphicsContext gc = canvas.getGraphicsContext2D();

            gc.setFill(Color.RED);
            gc.setFont(new Font("Arial", letterSize));

            if (i < 6 && i > 0) {
                gc.fillText(String.valueOf(wrongLetter[i]), 0+letterSpacing+ canvas.getWidth()*0.3, rowOne);
            }
            if (i < 11 && i > 5) {
                gc.fillText(String.valueOf(wrongLetter[i]), 0+letterSpacing+ canvas.getWidth()*0.3, rowTwo);
            }


            counter++;
            if (counter > 4) {
                counter = 0;
            }
        }
    }

    public void addCorrectLetter() {


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
        for (int i = 0; wordCount > i; i++) {

            if (correctGuess <= wordCount ) {
                gc.fillText(String.valueOf(correctLetter[i]),barSize*i*1.5, canvas.getHeight()*0.8,barSize);
            }
        }

    }
    public void onEnterPressed(ActionEvent event) {

        int counter = 0;
        boolean foundMatch = false;

        // Get user input
        String guessWord = guessField.getText().toUpperCase();
        char userInput = guessWord.charAt(0);
        // process the user input here...
        char[] letters = userWord.toCharArray();
        guessField.clear();

        if (guessWord.length() > 1) {
            if (guessWord.equals(userWord)) {

                counter = wordCount;
                for (int i = 0; i < letters.length; i++) {

                    if (letters[i] == guessWord.charAt(i)) {
                        // If a match is found, print the index and character
                        System.out.println("Found a match at index " + i + ": " + letters[i]);
                        correctLetter[i] = letters[i];

                    }
                }
            }

        } else {

            // Check if the user input is in the array of letters
            for (int i = 0; i < letters.length; i++) {

                if (letters[i] == userInput) {
                    // If a match is found, print the index and character
                    System.out.println("Found a match at index " + i + ": " + letters[i]);
                    //replace '*' with correct letter

                    correctLetter[i] = letters[i];
                    foundMatch = true;

                }
            }
            // Check if the user has lost
            if (!foundMatch) {
                wrongGuesses++;

                wrongLetter[wrongGuesses] = userInput;
                if (wrongGuesses >= 11) {
                    System.out.println("GAME OVER");
                }
            }

        }
        if (foundMatch) {

            for (int i = 0; i < letters.length; i++) {

                if (correctLetter[i] != '*') {
                    counter++;

                }
            }
        }

        // Check if the player has won
        if (counter == wordCount) {
            System.out.println("YOU WIN");
        }


        canvasBackground();


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
    private GameManager gameManager;
    private ObservableList<IGame> gameList;

    /**
     * Create game clicked.
     * @param event
     */
    @FXML
    public void onCreateButtonClick(ActionEvent event) {

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
                //gameListView.setDisable(!enabled);
                joinButton.setDisable(!enabled);
            }
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

        System.out.println("Initialized");
        for (int index = 0; index < wordCount; index++) {
            correctLetter[index] = '*';
        }
        //Keeps the canvas size updated
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> canvasBackground());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> canvasBackground());


        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        //setUIState(false, UISection.Join);
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
