package yh.fabulousstars.hangman.gui;

import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GameStage extends Stage {

    private static final double CANVAS_WIDTH = 500;
    private static final double CANVAS_HEIGHT = 400;

    class CanvasWrapper {
        Canvas canvas;
        IPlayer player;

        int guesses = 0;
        int correctGuess = 0;
    }
    private IGame game;
    private Map<String, CanvasWrapper> canvasMap;
    private Image[] stateImages;

    public GameStage(IGame game)
    {
        this.game = game;
        canvasMap = new HashMap<>();
        initOwner(GameApplication.getAppStage());
        setTitle("Hangman");

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

        VBox root = new VBox();
        HBox row = new HBox();

        int i = 0;
        var players = game.getPlayers();
        for(var player : players) {
            // new row
            if(i++ == 3) {
                i=0;
                root.getChildren().add(row);
                row = new HBox();
            }
            var wrapper = new CanvasWrapper();
            wrapper.player = player;
            wrapper.canvas = new CanvasClass();
            wrapper.canvas.setWidth(CANVAS_WIDTH);
            wrapper.canvas.setHeight(CANVAS_HEIGHT);
            wrapper.canvas.widthProperty().addListener(this::onCanvasSize);
            wrapper.canvas.heightProperty().addListener(this::onCanvasSize);
            canvasMap.put(player.getClientId(), wrapper);
            row.getChildren().add(wrapper.canvas);
        }
        root.getChildren().add(row);

        var scene = new Scene(root);
        setScene(scene);
        show();
    }

    private void onCanvasSize(Observable observable) {
        canvasMap.forEach((k,v) -> {
            canvasBackground(v);
        });
    }

    public void canvasBackground(CanvasWrapper wrapper) {
        //sout is used to check if the method is initialized
        //System.out.println("initialize method called");

        //gc = set the background color
        //creating a rectangle covering 100% of the canvas makes it look like a background
        //The color is able to change
        GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();
        gc.setFill(Color.WHEAT);
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
        int wordCount = 7;
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
        GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();
        // i = the amount of wrong guesses
        int i = wrapper.guesses;

        /*NOTE to self
         *Make own images
         * 1 for each state the hangman can be in
         * make them with transparent background
         * experiment with what a good size is
         * */
        if(i>0) {
            gc.drawImage(stateImages[i-1], 0, 0, wrapper.canvas.getWidth() * 0.3, wrapper.canvas.getHeight() * 0.5);
        }
    }

    public void addWrongLetter(CanvasWrapper wrapper) {
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
        int maxLetterSize = 80;
        int letterSize = (int) (wrapper.canvas.getWidth()*0.01* wrapper.canvas.getHeight()*0.02);

        if (letterSize > maxLetterSize) {
            letterSize = maxLetterSize;
        }

        int rowOne = letterSize;
        int rowTwo = letterSize*2;
        int rowThree = letterSize*3;
        int rowFour = letterSize*4;
        //change the "A" to the players input
        for (int i = 0; i < wrapper.guesses+1; i++) {
            int letterSpacing = counter*letterSize;

            GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();

            gc.setFill(Color.RED);
            gc.setFont(new Font("Arial", letterSize));

            if (i < 6 && i > 0) {
                gc.fillText("A", 0+letterSpacing+ wrapper.canvas.getWidth()*0.3, rowOne);
            }
            if (i < 11 && i > 5) {
                gc.fillText("B", 0+letterSpacing+ wrapper.canvas.getWidth()*0.3, rowTwo);
            }
            if (i < 16 && i > 10) {
                gc.fillText("C", 0+letterSpacing+ wrapper.canvas.getWidth()*0.3, rowThree);
            }
            if (i < 21 && i > 15) {
                gc.fillText("D", 0+letterSpacing+ wrapper.canvas.getWidth()*0.3, rowFour);
            }

            counter++;
            if (counter > 4) {
                counter = 0;
            }
        }
    }
    public void addCorrectLetter(CanvasWrapper wrapper) {
        //Temporary until the proper word count can be used
        int wordCount = 7;
        int maxBarSize = 60;
        int barWidth = (int) (wrapper.canvas.getWidth()*0.01);
        int barHeight = (int) (wrapper.canvas.getHeight()*0.02);
        int barSize = barWidth*barHeight;

        if (barSize > maxBarSize) {
            barSize = maxBarSize;
        }
        int maxLetterSize = 80;
        int letterSize = (int) (wrapper.canvas.getWidth()*0.01* wrapper.canvas.getHeight()*0.02);

        if (letterSize > maxLetterSize) {
            letterSize = maxLetterSize;
        }
        GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();
        gc.setFill(Color.GREEN);
        gc.setFont(new Font("Arial", letterSize));

        //Prints the image same amount of times as a word has letters
        for (int i = 0; wrapper.correctGuess > i; i++) {

            if (wrapper.correctGuess <= wordCount ) {
                gc.fillText("E",barSize*i*1.5, wrapper.canvas.getHeight()*0.8,barSize);
            }
        }
        if (wrapper.correctGuess == wordCount){
            //TODO: YOU WIN
        }

    }
}

