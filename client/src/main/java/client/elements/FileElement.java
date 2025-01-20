package client.elements;

import client.scenes.MainCtrl;
import client.utils.DialogUtil;
import client.utils.ServerUtils;
import commons.FileEntity;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileElement extends BorderPane {

    private final MainCtrl mainCtrl;
    private final ServerUtils server;
    private final DialogUtil dialogUtil;

    private final FileEntity file;

    public FileElement(MainCtrl mainCtrl, ServerUtils server, DialogUtil dialogUtil,
                       FileEntity file) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.dialogUtil = dialogUtil;
        this.file = file;

        Hyperlink label = new Hyperlink(file.getName());
        label.setAlignment(Pos.CENTER);
        label.setOnAction(_ -> {
            label.setVisited(false);
            this.promptDownload();
        });
        label.setPadding(new Insets(0, 5, 0, 0));

        Image deleteImage = new Image("appIcon/delete_icon.png");
        ImageView deleteIcon = new ImageView(deleteImage);
        deleteIcon.setPreserveRatio(true);
        deleteIcon.setFitHeight(10);

        Button deleteButton = new Button();
        deleteButton.setAlignment(Pos.CENTER);
        deleteButton.setMaxHeight(10);
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setOnAction(_ -> this.promptDelete());

        this.setCenter(label);
        this.setRight(deleteButton);
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

        // TODO: provide user feedback
        try (FileOutputStream out = new FileOutputStream(saveAt);
             ByteArrayInputStream in = new ByteArrayInputStream(file.getData())) {
            in.transferTo(out);
        } catch (IOException ex) {
            System.err.println("Failed to save note file to disk");
            ex.printStackTrace();
        }
    }

    private void promptDelete() {
        // TODO: prompt before deleting
        this.delete();
    }

    private void delete() {
        // TODO: provide user feedback
        server.deleteFile(file);
    }

}
