package yh.fabulousstars.hangman.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import yh.fabulousstars.hangman.MediaHelper;
import yh.fabulousstars.hangman.client.IGame;
import yh.fabulousstars.hangman.client.IPlayer;
import yh.fabulousstars.hangman.client.events.*;

import java.net.URL;
import java.util.*;

public class GameController implements Initializable {
    private static final int IMAGE_STATES = 11;
    private static final double CANVAS_WIDTH = 480;
    private static final double CANVAS_HEIGHT = 360;
    private IGame game;
    private boolean isRunning;
    private final ObservableList<String> chatList;
    private final ObservableList<String> playerList;
    private final Map<String, CanvasWrapper> canvasMap;
    private final MediaHelper media;
    private final MediaPlayer music;
    @FXML
    public TextField guessTextField;
    @FXML
    public ListView playerListView;
    @FXML
    public ListView chatListView;
    @FXML
    public TextField chatTextField;
    @FXML
    public Pane canvasContainer;
    @FXML
    public Button startButton;
    @FXML
    public VBox rootView;

    public GameController() {
        this.canvasMap = new HashMap<>();
        this.game = null;
        this.isRunning = false;
        this.chatList = FXCollections.observableArrayList();
        this.playerList = FXCollections.observableArrayList();
        this.media = MediaHelper.getInstance();
        this.music = this.media.getMedia("8-bit-brisk-music-loop");
        this.music.setVolume(0.3);
        this.music.setCycleCount(Integer.MAX_VALUE);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startButton.setDisable(true);
        startButton.setOnAction(this::onStartButton);
        guessTextField.setOnAction(this::onGuessEntered);
        guessTextField.setDisable(true);
        chatTextField.setOnAction(this::onChatEntered);
        chatListView.setItems(chatList);
        playerListView.setItems(playerList);

    }

    private void onGuessEntered(ActionEvent actionEvent) {
        var guess = guessTextField.getText().trim();
        guessTextField.clear();
        if (guess.length() == 1) {
            // send to server
            game.getManager().getClient().submitGuess(guess);
        } else {
            media.getSound("error").play();
        }
    }

    private void onStartButton(ActionEvent actionEvent) {
        startButton.setVisible(false);
        game.start();
    }

    private void onChatEntered(ActionEvent actionEvent) {
        var message = chatTextField.getText().trim();
        chatTextField.clear();
        if(!message.isEmpty()) {
            game.getManager().getClient().say(message);
        }
    }

    public void setGame(IGame game) {
        this.game = game;
        Platform.runLater(() -> {
            // initial player (this client)
            var initialPlayers = Arrays.asList(game.getManager().getClient());
            updatePlayers(initialPlayers);
        });

    }

    public void handlePlayerState(PlayerState event) {
        var wrapper = canvasMap.get(event.getState().getClientId());
        canvasBackground(wrapper);
    }

    public void handleGameStarted(GameStarted event) {
        startButton.setVisible(false);
        music.play();
        drawCanvases();
    }

    public void handleSubmitGuess(SubmitGuess event) {
        if(event.isCorrect()) {
            media.getSound("button").play();
        } else {
            media.getSound("error").play();
        }
    }

    public void handlePlayerList(PlayerList event) {

        System.out.println(event);

        var players = event.getPlayerList();
        updatePlayers(event.getPlayerList());
        startButton.setDisable(players.size() < 2);
    }

    public void handleChatMessage(ChatMessage event) {
        chatList.add(0, event.getMessage());
        media.getSound("button").play();
    }

    /**
     * Rebuild canvas grid.
     *
     * @param players
     */
    private void updatePlayers(List<IPlayer> players) {
        var clientIds = new ArrayList<String>();
        for (var player : players) {
            clientIds.add(player.getClientId());
            if (!canvasMap.containsKey(player.getClientId())) {
                var canvas = new Canvas();
                var wrapper = new CanvasWrapper(canvas, player);
                canvasMap.put(player.getClientId(), wrapper);
            }
        }
        // remove obsolete players
        var keys = new ArrayList<>(canvasMap.keySet());
        for (var key : keys) {
            if (!clientIds.contains(key)) {
                canvasMap.remove(key);
            }
        }
        // rebuild canvas grid
        int count = players.size();
        int row = 0;
        int col = -1;
        canvasContainer.getChildren().clear();
        for (var wrapper : canvasMap.values()) {
            if(++col > 2) {
                col = 0;
                row++;
            }
            canvasContainer.getChildren().add(wrapper.canvas);
            wrapper.canvas.setLayoutY(CANVAS_HEIGHT * row);
            wrapper.canvas.setLayoutX(CANVAS_WIDTH * col);
            wrapper.canvas.resize(CANVAS_WIDTH, CANVAS_HEIGHT);
        }
        canvasContainer.resize(CANVAS_WIDTH * col, CANVAS_HEIGHT * row);
        var scene = rootView.getScene();
        rootView.layout();
        scene.getWindow().sizeToScene();
        drawCanvases();
    }

    private void drawCanvases() {
        canvasMap.forEach((client, wrapper) -> {
            canvasBackground(wrapper);
        });
    }

    public void canvasBackground(CanvasWrapper wrapper) {

        //gc = set the background color
        //creating a rectangle covering 100% of the canvas makes it look like a background
        //The color is able to change
        GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();

        // if self
        var local = game.getManager().getClient().getClientId();
        var wrapped = wrapper.player.getClientId();
        if(local == wrapped) {
            gc.setFill(Color.LIGHTSKYBLUE);
        } else {
            gc.setFill(Color.LIGHTCORAL);
        }

        gc.fillRect(0, 0, wrapper.canvas.getWidth(), wrapper.canvas.getHeight());

        if(wrapper.player.getPlayState() != null) {
            //Prints the black bar
            blackBarForLetter(wrapper);
            //Draws the hangman
            hangmanFigure(wrapper);
            //draws the wrongly guessed letters
            addWrongLetter(wrapper);
            //draws the correctly guessed word
            addCorrectLetter(wrapper);
        }
    }

    /**
     * Draw letter positions.
     *
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
            gc.drawImage(media.getImage("BlackBarTR"), barSize * i * 1.5, wrapper.canvas.getHeight() * 0.8, barSize, wrapper.canvas.getHeight() * 0.01);
        }
    }

    /**
     * Draw figure based on damage.
     *
     * @param wrapper
     */
    public void hangmanFigure(CanvasWrapper wrapper) {

        GraphicsContext gc = wrapper.canvas.getGraphicsContext2D();
        var state = wrapper.player.getPlayState();
        var damage = state == null ? 1 : state.getTotalDamage()+1;
        if (damage > IMAGE_STATES) {
            damage = IMAGE_STATES;
        }
        gc.drawImage(media.getImage("HangmanTranState"+damage),
                0, 10,
                wrapper.canvas.getWidth() * 0.3, wrapper.canvas.getHeight() * 0.5);
    }

    public void addWrongLetter(CanvasWrapper wrapper) {
        /*
         * if the guess is wrong make the letter appear in red
         * place them to the right of the hangman
         * have a method to check if letter is correct
         * if wrong print it on the canvas
         */
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
        var wordLength = wrapper.player.getPlayState().getCurrentWord().length();
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
     * Request word from player.
     * Send it to server.
     *
     * @param event
     */
    public void handleRequestWord(RequestWord event) {
        media.getSound("button").play();
        String word = null;
        while (word == null) {
            word = DialogHelper.promptString(String.format(
                    "Enter new word between %d and %d letters:",
                    event.getMinLength(),
                    event.getMaxLength()
            ));
            if (word != null) {
                word = word.strip();
                if (word.length() >= event.getMinLength()
                        && word.length() <= event.getMaxLength()) {
                    break;
                }
            }
        }
        // send to server
        game.getManager().getClient().submitWord(word);
    }

    public void handleRequestGuess(RequestGuess event) {
        guessTextField.setDisable(false);
    }

    public void dispose() {
        music.stop();
    }

    class CanvasWrapper {
        final Canvas canvas;
        final IPlayer player;
        final ArrayList<Character> correctLetters;
        final ArrayList<Character> wrongLetters;

        CanvasWrapper(Canvas canvas, IPlayer player) {
            this.canvas = canvas;
            this.player = player;
            this.correctLetters = new ArrayList<>();
            this.wrongLetters = new ArrayList<>();
        }
    }

}
