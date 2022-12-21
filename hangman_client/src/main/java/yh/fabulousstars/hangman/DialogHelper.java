package yh.fabulousstars.hangman;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

public final class DialogHelper {
    public static boolean showMessage(String message, Alert.AlertType type) {
        Alert alertWindow = new Alert(type);
        alertWindow.setTitle(type.toString());
        alertWindow.setContentText(message);
        var res = alertWindow.showAndWait();
        return res.get().equals(ButtonType.OK);
    }

    public static String promptString(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input");
        dialog.setContentText(prompt);
        var result = dialog.showAndWait();
        return result.isPresent() ? result.get() : null;
    }
}
