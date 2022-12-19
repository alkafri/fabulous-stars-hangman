package yh.fabulousstars.hangman;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class GameApplication extends Application {
    private static Stage appStage = null;
    public static Stage getAppStage() {
        return appStage;
    }

    @Override
    public void start(Stage stage) throws IOException {
        appStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(GameApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), Color.GRAY);
        stage.setTitle("Hangman!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
