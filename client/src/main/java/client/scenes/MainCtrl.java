/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ResourceBundle;

public class MainCtrl {

    private Stage primaryStage;
    private Scene collectionEdit;

    private Image appIcon;

    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // The icon is taken from a google search TODO:Create our own icon
        this.appIcon = new Image("appIcon/NoteIcon.jpg");
        primaryStage.getIcons().add(appIcon);
        primaryStage.setTitle("NetNote");
        primaryStage.show();
    }

    public void loadScenes(Pair<NoteEditCtrl, Parent> noteEdit,
                           Pair<CollectionEditCtrl, Parent> collectionEdit) {
        NoteEditCtrl noteEditCtrl = noteEdit.getKey();
        Scene overview = new Scene(noteEdit.getValue());

        primaryStage.setScene(overview);
        noteEditCtrl.refresh();

        this.collectionEdit = new Scene(collectionEdit.getValue());
    }

    public void showCollectionEdit(ResourceBundle resourceBundle) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(this.primaryStage);
        popupStage.getIcons().add(this.appIcon);
        popupStage.setTitle(resourceBundle.getString("window.editCollections"));
        popupStage.setScene(this.collectionEdit);
        popupStage.show();
    }

}