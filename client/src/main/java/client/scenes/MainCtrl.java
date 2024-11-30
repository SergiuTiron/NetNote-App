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
import javafx.stage.Stage;
import javafx.util.Pair;

public class MainCtrl {

    private Stage primaryStage;

    private NoteEditCtrl noteEditCtrl;

    private Scene overview;

    public void initialize(Stage primaryStage, Pair<NoteEditCtrl, Parent> noteEdit) {
        this.primaryStage = primaryStage;
        this.noteEditCtrl = noteEdit.getKey();
        this.overview = new Scene(noteEdit.getValue());

        showOverview();
        primaryStage.show();
    }



    public void showOverview() {
        //The icon is taken from a google search TODO:Create our own icon
        Image image = new Image("appIcon/NoteIcon.jpg");
        primaryStage.getIcons().add(image);
        primaryStage.setTitle("NetNote");
        primaryStage.setScene(overview);
        noteEditCtrl.refresh();
    }

}