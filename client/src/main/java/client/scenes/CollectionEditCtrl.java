package client.scenes;

import client.Config;
import client.ConfigManager;
import client.utils.ServerUtils;
import commons.Collection;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class CollectionEditCtrl implements Initializable {
    private final ServerUtils server;
    private final Config config;
    private final ConfigManager configManager;
    private final NoteEditCtrl noteEditCtrl;

    public final ObjectProperty<Locale> selectedLanguage = new SimpleObjectProperty<>();

    @FXML
    private ListView<Collection> collectionListView;

    @Inject
    public CollectionEditCtrl(ServerUtils server, NoteEditCtrl noteEditCtrl, Config config, ConfigManager configManager) {
        this.server = server;
        this.noteEditCtrl = noteEditCtrl;
        this.config = config;
	    this.configManager = configManager;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        collectionListView.setEditable(true);

        // Retrieve all collections from server and add them to listView
        List<Collection> collections = server.getCollections();

        // Create default collection if not present
        Collection defaultCollection = server.getDefaultCollection();
        if (!collections.contains(defaultCollection)) {
            collections.addFirst(defaultCollection); // Add default collection to the beginning or wherever you prefer
        }
        config.setDefaultCollection(defaultCollection);
        try {
            configManager.saveConfig(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        collectionListView.setItems(FXCollections.observableList(collections));

        collectionListView.setCellFactory(_ -> new TextFieldListCell<>(new StringConverter<>() {
            @Override
            public String toString(Collection collection) {
                if (collection == null) return "";
                return collection.getName();
            }

            @Override
            public Collection fromString(String newName) {
                Collection selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
                if (selectedCollection != null){
                    if(selectedCollection.equals(defaultCollection)){
                        System.err.println("Default collection's name cannot be changed");
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Default collection warning");
                        alert.setHeaderText("Default collection's name cannot be changed");
                        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                        alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
                        alert.setContentText(null);
                        alert.getDialogPane();
                        alert.showAndWait();
                        return selectedCollection;
                    }

                    if(newName.isBlank()){
                        System.err.println("Collection name must not be empty.");
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Collection name warning");
                        alert.setHeaderText("Collection name cannot be empty");
                        alert.setContentText("Please try to choose a proper collection title.");
                        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                        alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
                        alert.getDialogPane();
                        alert.showAndWait();
                        return selectedCollection;
                    }

                    //ensure titles are unique
                    if(collections.stream()
                            .anyMatch(collection -> collection.getName().equals(newName)
                                    && !collection.equals(selectedCollection)))
                    {
                        System.err.println("Collection name must be unique.");
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Collection name warning");
                        alert.setHeaderText("There is already a title with the given name");
                        alert.setContentText("Please try to choose a unique collection title.");
                        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                        alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
                        alert.getDialogPane();
                        alert.showAndWait();
                        return selectedCollection;
                    }

                    selectedCollection.setName(newName.strip());
                    server.addCollection(selectedCollection);
                    System.out.println("Collection title changed");
                    refresh();
                }
                return selectedCollection;
            }
        }));

        collectionListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int index = collectionListView.getSelectionModel().getSelectedIndex();
                if (index != -1) {
                    collectionListView.edit(index);
                }
            }
        });

        Platform.runLater(() -> {
            for (Collection collection : config.getCollections()) {
                collections.add(collection);
                noteEditCtrl.addCollectionToMenuButton(collection);
            }
        });
    }

    /**
     * Method for creating a new collection based on user input
     */
    public void createCollection() {
        // Create a TextInputDialog for entering the collection name
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Collection");
        dialog.setHeaderText("Enter the name of the new collection:");
        dialog.setContentText("Collection name:");

        // Show the dialog and wait for a response
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(collectionName -> {
            if(collectionName.isBlank()){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Collection name warning");
                alert.setHeaderText("Collection name cannot be empty");
                alert.setContentText("Please try to choose a proper collection title.");
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
                alert.getDialogPane();
                alert.showAndWait();
                return;
                //throw new IllegalArgumentException("Collection name cannot be blank.");

            }

            List<Collection> existingCollections = server.getCollections();
            if(existingCollections
                    .stream()
                    .anyMatch(collection -> collection.getName()
                            .equals(collectionName.strip())))
            {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Collection name warning");
                    alert.setHeaderText("There is already a title with the given name");
                    alert.setContentText("Please try to choose a unique collection title.");
                    Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
                    alert.getDialogPane();
                    alert.showAndWait();
                    return;
                    //throw new IllegalArgumentException("Collection name cannot be duplicated.");
                }
            // Add collection to server
            Collection collection = new Collection(collectionName);
            Collection savedCollection = server.addCollection(collection);
            config.addCollection(savedCollection);
            try {
                configManager.saveConfig(config);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Add collection to listView
            collectionListView.getItems().add(savedCollection);
            collectionListView.getSelectionModel().select(savedCollection);
            // Add collection to MenuButton
            noteEditCtrl.addCollectionToMenuButton(savedCollection);
            refresh();
            System.out.println("Collection created successfully");
        });


    }

    /**
     * Delete selected collection
     */
    public void deleteCollection() throws IOException {
        Collection selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
        if(selectedCollection != null) {
            try {
                server.deleteCollection(selectedCollection.getId());
                config.removeCollection(selectedCollection);
                try {
                    configManager.saveConfig(config);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                collectionListView.getItems().remove(selectedCollection);
                noteEditCtrl.deleteCollectionToMenuButton(selectedCollection);
                refresh();
                System.out.println("Collection deleted successfully");
            } catch (IOException e) {
                System.err.println("Failed to delete collection");
                e.printStackTrace();
            }
        }
        else {
            System.err.println("Delete attempt with no collection selected");
        }
    }

    public void refresh() {
        List<Collection> collections = server.getCollections();
        System.out.println(collections.toString());
        collectionListView.setItems(FXCollections.observableList(collections));
        System.out.println("Collection refreshed");
    }

    /**
     * Find a given collection by its id
     * @param name - name of collection to find
     * @return - id of collection
     */
    private Collection findCollectionById(String name) {
        List<Collection> collections = server.getCollections();

        Collection collectionToFind = collections.stream()
                .filter(collection -> collection.getName().equalsIgnoreCase(name))
                .findAny().orElse(null);

        if (collectionToFind == null) {
            throw new RuntimeException("Collection not found");
        } else {
            return collectionToFind;
        }
    }

    public void setLanguage(Locale locale) {
        this.selectedLanguage.setValue(locale);
    }
}
