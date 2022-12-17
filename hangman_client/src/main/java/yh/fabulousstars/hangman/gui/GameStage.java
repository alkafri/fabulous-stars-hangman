package yh.fabulousstars.hangman.gui;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;
import yh.fabulousstars.hangman.GameApplication;
import yh.fabulousstars.hangman.client.IGame;
import yh.fabulousstars.hangman.client.IPlayer;
import yh.fabulousstars.hangman.client.events.*;

import java.util.*;
import java.util.stream.Collectors;

public class GameStage extends Stage {

    private static final double CANVAS_WIDTH = 500;
    private static final double CANVAS_HEIGHT = 400;

    public void handlePlayerJoined(PlayerJoined event) {
    }

    public void handlePlayerLeft(PlayerLeft event) {
    }

    public void handlePlayerState(PlayerState.State state) {
    }

    public void handleGameStarted(GameStarted event) {
    }

    public void handleSubmitWord(SubmitWord event) {
    }

    public void handleSubmitGuess(SubmitGuess event) {
    }

    public void handlePlayerList(PlayerList event) {
    }

    public void handleChatMessage(ChatMessage event) {
    }

    class CanvasWrapper {
        Canvas canvas;
        IPlayer player;

        int guesses = 0;
        int correctGuess = 0;
    }
    private final IGame game;
    private final Map<String, CanvasWrapper> canvasMap;
    private final Image[] stateImages;
    private final VBox canvasRows;
    private final TextField guessField;

    public GameStage(IGame game)
    {
        this.game = game;
        canvasMap = new HashMap<>();
        initOwner(GameApplication.getAppStage());
        setTitle("Hangman");

        // load images
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
        stateImages = new Image[images.size()];
        for (int i = 0; i <stateImages.length; i++) {
            stateImages[i] = new Image(images.get(i));
        }

        // root layout
        VBox root = new VBox();

        // canvases container
        canvasRows = new VBox();
        root.getChildren().add(canvasRows);

        // initial player (this client)
        var initialPlayers = Arrays.asList(game.getManager().getClient());
        updatePlayers(initialPlayers);

        guessField = new TextField();
        guessField.setOnAction(this::onEnterPressed);
        guessField.setEditable(false);

        var scene = new Scene(root);
        setScene(scene);
        show();
    }

    /**
     * Rebuild canvas grid.
     * @param players
     */
    private void updatePlayers(List<IPlayer> players) {
        for(var player : players) {
            if(!canvasMap.containsKey(player.getClientId())) {
                var wrapper = new CanvasWrapper();
                wrapper.player = player;
                wrapper.canvas = new CanvasClass();
                wrapper.canvas.setWidth(CANVAS_WIDTH);
                wrapper.canvas.setHeight(CANVAS_HEIGHT);
                wrapper.canvas.widthProperty().addListener(this::onCanvasSize);
                wrapper.canvas.heightProperty().addListener(this::onCanvasSize);
                canvasMap.put(player.getClientId(), wrapper);
            }
        }
        // remove obsolete players
        List<String> clients = players.stream().map(player -> player.getClientId()).collect(Collectors.toList());
        for(var key : canvasMap.keySet()) {
            if(!clients.contains(key)) {
                canvasMap.remove(key);
            }
        }
        // rebuild canvas grid
        canvasRows.getChildren().clear();
        var canvasCols = new HBox();
        int i = 0;
        var player = game.getManager().getClient();
        for(var wrapper : canvasMap.values()) {
            // new row
            if(i++ == 3) {
                i=0;
                canvasRows.getChildren().add(canvasCols);
                canvasCols = new HBox();
            }
            canvasCols.getChildren().add(wrapper.canvas);
        }
        canvasRows.getChildren().add(canvasCols);
        canvasRows.requestLayout();
    }

    private void onCanvasSize(Observable observable) {
        canvasMap.forEach((client, wrapper) -> {
            canvasBackground(wrapper);
        });
    }


    public void canvasBackground(CanvasWrapper wrapper) {

        //gc = set the background color
        //creating a rectangle covering 100% of the canvas makes it look like a background
        //The color is able to change
        GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTSKYBLUE);
        gc.fillRect(0, 0, wrapper.canvas.getWidth(), wrapper.canvas.getHeight());


        //Prints the black bar
        blackBarForLetter(wrapper);
        //Draws the hangman
        hangmanFigure(wrapper);
        //draws the wrongly guessed letters
        addWrongLetter(wrapper);
        //draws the correctly guessed word
        addCorrectLetter(wrapper);
    }
    public void blackBarForLetter(CanvasWrapper wrapper) {
        //Temporary until the proper word count can be used
        int maxBarSize = 60;
        int barWidth = (int) (wrapper.canvas.getWidth()*0.01);
        int barHeight = (int) (wrapper.canvas.getHeight()*0.02);
        int barSize = barWidth*barHeight;

        if (barSize > maxBarSize) {
            barSize = maxBarSize;
        }
        GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();

        //Prints the image same amount of times as a word has letters
        for (int i = 0; wordCount > i; i++) {

            Image image = new Image("BlackBarTR.png");
            gc.drawImage(image,barSize*i*1.5, wrapper.canvas.getHeight()*0.8,barSize,wrapper.canvas.getHeight()*0.01);
        }
    }
    public void hangmanFigure(CanvasWrapper wrapper) {

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

}

