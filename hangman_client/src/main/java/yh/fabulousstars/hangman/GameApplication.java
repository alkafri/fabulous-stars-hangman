package yh.fabulousstars.hangman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class GameApplication extends Application {
    private static Stage appStage = null;

    public static Stage getAppStage() {
        return appStage;
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        appStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(GameApplication.class.getResource("/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), Color.GRAY);
        stage.setTitle("Hangman!");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
