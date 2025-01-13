package client.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.ResourceBundle;

public class DialogUtil {

    public Optional<ButtonType> showDialog(ResourceBundle resourceBundle, AlertType alertType,
                                           String translationKey, ButtonType... buttonTypes) {
        Alert alert = new Alert(alertType, resourceBundle.getString(translationKey + ".title"));
        alert.setContentText(resourceBundle.getString(translationKey + ".text"));

        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));

        return alert.showAndWait();
    }

}
