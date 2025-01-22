package client.elements;

import client.scenes.MainCtrl;
import client.scenes.NoteEditCtrl;
import client.utils.DialogUtil;
import client.utils.ServerUtils;
import commons.FileEntity;
import commons.Note;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

public class FileElement extends BorderPane {

    private final MainCtrl mainCtrl;
    private final NoteEditCtrl noteEditCtrl;
    private final ServerUtils server;
    private final DialogUtil dialogUtil;
    private final ResourceBundle resourceBundle;

    private final FileEntity file;

    public FileElement(MainCtrl mainCtrl, NoteEditCtrl noteEditCtrl, ServerUtils server, DialogUtil dialogUtil,
                       ResourceBundle resourceBundle, FileEntity file) {
        this.mainCtrl = mainCtrl;
        this.noteEditCtrl = noteEditCtrl;
        this.server = server;
        this.dialogUtil = dialogUtil;
        this.resourceBundle = resourceBundle;
        this.file = file;

        Hyperlink label = new Hyperlink(file.getName());
        label.setAlignment(Pos.CENTER);
        label.setOnAction(_ -> {
            label.setVisited(false);
            this.promptDownload();
        });
        label.setPadding(new Insets(0, 5, 0, 0));
        label.setMaxWidth(200);

        Image deleteImage = new Image("appIcon/delete_icon.png");
        ImageView deleteIcon = new ImageView(deleteImage);
        deleteIcon.setPreserveRatio(true);
        deleteIcon.setFitHeight(10);

        Button deleteButton = new Button();
        deleteButton.setAlignment(Pos.CENTER);
        deleteButton.setMaxHeight(10);
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setOnAction(_ -> this.promptDelete());

        Image changeNameImage = new Image("appIcon/changeName.png");
        ImageView changeNameIcon = new ImageView(changeNameImage);
        changeNameIcon.setPreserveRatio(true);
        changeNameIcon.setFitHeight(10);

        Button changeName = new Button();
        changeName.setAlignment(Pos.CENTER);
        changeName.setMaxHeight(10);
        changeName.setGraphic(changeNameIcon);
        changeName.setOnAction(_ -> this.updateFileTitle());
        this.setCenter(label);
        this.setRight(changeName);
        this.setRight(deleteButton);
    }

    private void updateFileTitle() {
        Long id = file.getId();
        Note currentNote = noteEditCtrl.getCurrentNote();
        currentNote.getFiles().get(id.intValue()).setName("bbb");
    }

    private void promptDownload() {
        File saveAt = mainCtrl.promptFileSave(file.getName());
        if (saveAt == null) {
            // User cancelled operation
            return;
        }
        this.downloadTo(saveAt);
    }

    private void downloadTo(File saveAt) {
        System.out.println("Downloading file " + file.getName() + " (" + file.getId() + ") to "
                + saveAt.getAbsolutePath());

        try (FileOutputStream out = new FileOutputStream(saveAt);
             ByteArrayInputStream in = new ByteArrayInputStream(file.getData())) {
            in.transferTo(out);

            dialogUtil.showDialog(this.resourceBundle, AlertType.INFORMATION,
                    "popup.files.downloaded");
        } catch (IOException ex) {
            System.err.println("Failed to save note file to disk");
            ex.printStackTrace();

            dialogUtil.showDialog(this.resourceBundle, AlertType.ERROR,
                    "popup.files.downloadFailed");
        }
    }

    private void promptDelete() {
        Optional<ButtonType> response = dialogUtil.showDialog(this.resourceBundle, AlertType.CONFIRMATION,
                "popup.files.confirmDelete");
        if (response.isPresent() && response.get() == ButtonType.OK) {
            this.delete();
        }
    }

    private void delete() {
        server.deleteFile(file);
        noteEditCtrl.refresh();
        dialogUtil.showDialog(this.resourceBundle, AlertType.CONFIRMATION,
                "popup.files.deleted");
    }

}
