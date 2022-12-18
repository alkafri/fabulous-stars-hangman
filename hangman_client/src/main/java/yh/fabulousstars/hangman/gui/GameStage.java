package yh.fabulousstars.hangman.gui;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import yh.fabulousstars.hangman.GameApplication;
import yh.fabulousstars.hangman.client.IGame;
import yh.fabulousstars.hangman.client.IPlayer;
import yh.fabulousstars.hangman.client.events.*;

import java.util.*;
import java.util.stream.Collectors;

public class GameStage extends Stage {

    private static final double CANVAS_WIDTH = 500;
    private static final double CANVAS_HEIGHT = 400;
    private final IGame game;
    private final Map<String, CanvasWrapper> canvasMap;
    private final Image[] stateImages;
    private final VBox canvasRows;
    private final TextField guessField;
    private final ListView<String> chatListView;
    private final ObservableList<String> chatList;
    private final TextField chatField;

    private boolean isRunning;

    public GameStage(IGame game) {
        this.game = game;
        this.isRunning = false;
        this.canvasMap = new HashMap<>();
        this.chatList = FXCollections.observableArrayList();

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
        this.stateImages = new Image[images.size()];
        for (int i = 0; i < stateImages.length; i++) {
            this.stateImages[i] = new Image(images.get(i));
        }

        // root layout
        VBox root = new VBox();

        // canvases container
        this.canvasRows = new VBox();
        root.getChildren().add(canvasRows);

        // initial player (this client)
        var initialPlayers = Arrays.asList(game.getManager().getClient());
        updatePlayers(initialPlayers);

        // controls
        var guessLabel = new Label("Guess: ");
        this.guessField = new TextField();
        this.guessField.setPrefWidth(100);
        this.guessField.setOnAction(this::onEnterPressed);
        this.guessField.setDisable(true);

        var startButton = new Button("Start");
        startButton.setOnAction(this::onStartButton);

        var sep = new Separator();

        chatListView = new ListView<>();
        chatListView.setMaxWidth(1000);
        chatListView.setPrefHeight(100);
        chatField = new TextField();
        chatField.setOnAction(this::onLineTyped);
        chatField.setMaxWidth(1000);

        var controlsLine = new HBox();
        controlsLine.getChildren().addAll(guessLabel, this.guessField, sep, startButton);

        root.getChildren().addAll(controlsLine, chatListView, chatField);

        var scene = new Scene(root);

        setScene(scene);
        show();
    }

    private void onLineTyped(ActionEvent actionEvent) {
        // todo: chat
    }

    private void onStartButton(ActionEvent actionEvent) {
        if(actionEvent.getSource() instanceof Button) {
            ((Button)actionEvent.getSource()).setVisible(false);
            game.start();
        }
    }

    public void handlePlayerJoined(PlayerJoined event) {
        updatePlayers(game.getPlayers());
    }

    public void handlePlayerLeft(PlayerLeft event) {
        updatePlayers(game.getPlayers());
    }

    public void handlePlayerState(PlayerState event) {
        var wrapper = canvasMap.get(event.getState().getClientId());
        canvasBackground(wrapper);
    }

    public void handleGameStarted(GameStarted event) {
        for (var wrapper : canvasMap.values()) {
            canvasBackground(wrapper);
        }
    }

    public void handleSubmitGuess(SubmitGuess event) {
        //todo handleSubmitGuess
    }

    public void handlePlayerList(PlayerList event) {
    }

    public void handleChatMessage(ChatMessage event) {
    }

    /**
     * Rebuild canvas grid.
     *
     * @param players
     */
    private void updatePlayers(List<IPlayer> players) {
        for (var player : players) {
            if (!canvasMap.containsKey(player.getClientId())) {
                var canvas = new CanvasClass();
                canvas.setWidth(CANVAS_WIDTH);
                canvas.setHeight(CANVAS_HEIGHT);
                canvas.widthProperty().addListener(this::onCanvasSize);
                canvas.heightProperty().addListener(this::onCanvasSize);
                var wrapper = new CanvasWrapper(canvas, player);
                canvasMap.put(player.getClientId(), wrapper);
            }
        }
        // remove obsolete players
        List<String> clients = players.stream().map(player -> player.getClientId()).collect(Collectors.toList());
        for (var key : canvasMap.keySet()) {
            if (!clients.contains(key)) {
                canvasMap.remove(key);
            }
        }
        // rebuild canvas grid
        canvasRows.getChildren().clear();
        var canvasCols = new HBox();
        int i = 0;
        var player = game.getManager().getClient();
        for (var wrapper : canvasMap.values()) {
            // new row
            if (i++ == 3) {
                i = 0;
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

    /**
     * Draw letter positions.
     * @param wrapper
     */
    public void blackBarForLetter(CanvasWrapper wrapper) {
        int maxBarSize = 60;
        int barWidth = (int) (wrapper.canvas.getWidth() * 0.01);
        int barHeight = (int) (wrapper.canvas.getHeight() * 0.02);
        int barSize = barWidth * barHeight;
        if (barSize > maxBarSize) {
            barSize = maxBarSize;
        }
        GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();
        //Prints the image same amount of times as a word has letters
        var letterCount = wrapper.player.getPlayState().getCurrentWord().length();
        for (int i = 0; letterCount > i; i++) {
            Image image = new Image("BlackBarTR.png");
            gc.drawImage(image, barSize * i * 1.5, wrapper.canvas.getHeight() * 0.8, barSize, wrapper.canvas.getHeight() * 0.01);
        }
    }

    /**
     * Draw figure based on damage.
     * @param wrapper
     */
    public void hangmanFigure(CanvasWrapper wrapper) {

        GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();
        var state = wrapper.player.getPlayState();
        var damage = state==null ? 0 : state.getTotalDamage();
        if(damage >= stateImages.length) { damage = stateImages.length-1; }
        gc.drawImage(stateImages[damage], 0, 10, wrapper.canvas.getWidth() * 0.3, wrapper.canvas.getHeight() * 0.5);
    }

    public void addWrongLetter(CanvasWrapper wrapper) {
        /*
         * if the guess is wrong make the letter appear in red
         * place them to the right of the hangman
         * have a method to check if letter is correct
         * if wrong print it on the canvas
         */
        Scanner scanner = new Scanner(System.in);
        int counter = -1;
        int maxLetterSize = 100;
        int letterSize = (int) (wrapper.canvas.getWidth() * 0.01 * wrapper.canvas.getHeight() * 0.02);

        if (letterSize > maxLetterSize) {
            letterSize = maxLetterSize;
        }

        int rowOne = letterSize;
        int rowTwo = letterSize * 2;

        for (int i = 0; i < wrapper.wrongLetters.size() + 1; i++) {
            int letterSpacing = counter * letterSize;

            GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();

            gc.setFill(Color.RED);
            gc.setFont(new Font("Arial", letterSize));

            var letter = wrapper.wrongLetters.get(i).toString();
            if (i < 6 && i > 0) {
                gc.fillText(letter, 0 + letterSpacing + wrapper.canvas.getWidth() * 0.3, rowOne);
            }
            if (i < 11 && i > 5) {
                gc.fillText(letter, 0 + letterSpacing + wrapper.canvas.getWidth() * 0.3, rowTwo);
            }

            counter++;
            if (counter > 4) {
                counter = 0;
            }
        }
    }

    public void addCorrectLetter(CanvasWrapper wrapper) {


        int maxBarSize = 60;
        int barWidth = (int) (wrapper.canvas.getWidth() * 0.01);
        int barHeight = (int) (wrapper.canvas.getHeight() * 0.02);
        int barSize = barWidth * barHeight;

        if (barSize > maxBarSize) {
            barSize = maxBarSize;
        }
        int maxLetterSize = 80;
        int letterSize = (int) (wrapper.canvas.getWidth() * 0.01 * wrapper.canvas.getHeight() * 0.02);

        if (letterSize > maxLetterSize) {
            letterSize = maxLetterSize;
        }
        GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();
        gc.setFill(Color.GREEN);
        gc.setFont(new Font("Arial", letterSize));

        //Prints the image same amount of times as a word has letters
        var wordLength= wrapper.player.getPlayState().getCurrentWord().length();
        for (int i = 0; wordLength > i; i++) {

            if (wrapper.correctLetters.size() <= wordLength) {
                gc.fillText(wrapper.correctLetters.get(i).toString(),
                        barSize * i * 1.5,
                        wrapper.canvas.getHeight() * 0.8,
                        barSize);
            }
        }
    }

    /**
     * Handle guess input.
     * Send it to server.
     *
     * @param event
     */
    public void onEnterPressed(ActionEvent event) {
        var guess = guessField.getText().trim();
        guessField.clear();
        if(guess.length()==1) {
            // send to server
            game.getManager().getClient().submitGuess(guess);
        }
    }

    /**
     * Request word from player.
     * Send it to server.
     * @param event
     */
    public void handleRequestWord(RequestWord event) {
        String word = null;
        while(word==null) {
            word = DialogHelper.promptString(String.format(
                    "Enter new word between %d and %d letters:",
                    event.getMinLength(),
                    event.getMaxLength()
            ));
            if(word!=null) {
                word = word.strip();
                if(word.length() >= event.getMinLength()
                        && word.length() <= event.getMaxLength()) {
                    break;
                }
            }
        }
        // send to server
        game.getManager().getClient().submitWord(word);
    }

    public void handleRequestGuess(RequestGuess event) {
        guessField.setDisable(false);
    }

    class CanvasWrapper {
        final Canvas canvas;
        final IPlayer player;
        final ArrayList<Character> correctLetters;
        final ArrayList<Character> wrongLetters;

        public CanvasWrapper(Canvas canvas, IPlayer player) {
            this.canvas = canvas;
            this.player = player;
            this.correctLetters = new ArrayList<>();
            this.wrongLetters = new ArrayList<>();
        }
    }
}

