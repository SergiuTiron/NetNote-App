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
package client;

import static com.google.inject.Guice.createInjector;

import client.scenes.NoteEditCtrl;
import com.google.inject.Injector;

import client.scenes.MainCtrl;
import client.utils.ServerUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Locale;

public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private NoteEditCtrl noteEditCtrl;
    private MainCtrl mainCtrl;

    private Locale locale = DEFAULT_LOCALE;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        ServerUtils serverUtils = INJECTOR.getInstance(ServerUtils.class);
        if (!serverUtils.isServerAvailable()) {
            System.err.println("Server needs to be started before the client, but it does not seem to be available. Shutting down.");
            return;
        }

        this.mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage);
        this.loadScenes();

        primaryStage.setOnCloseRequest(_ -> {
            if (noteEditCtrl != null) {
                noteEditCtrl.saveChanges();
                System.out.println("Changes were saved on exit.");
            } else {
                System.err.println("Changes were not saved on exit.");
            }
        });
    }

    public void loadScenes() {
        Pair<NoteEditCtrl, Parent> editView = FXML.load(this.locale, "client", "scenes", "NoteEditView.fxml");
        this.noteEditCtrl = editView.getKey();
        noteEditCtrl.selectedLanguage.addListener(_ -> {
            this.locale = noteEditCtrl.selectedLanguage.get();
            System.out.println("Language changed to " + locale);
            this.loadScenes();
        });
        mainCtrl.showOverview(editView);
    }

}