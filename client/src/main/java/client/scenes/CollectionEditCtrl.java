package client.scenes;

import client.Config;
import client.ConfigManager;
import client.utils.DialogUtil;
import client.utils.ServerUtils;
import commons.Collection;
import jakarta.inject.Inject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class CollectionEditCtrl implements Initializable {
    private final ServerUtils server;
    private final DialogUtil dialogUtil;

    private final Config config;
    private final ConfigManager configManager;

    private final NoteEditCtrl noteEditCtrl;

    private Collection currentCollection;

    private ResourceBundle resourceBundle;
    public final ObjectProperty<Locale> selectedLanguage = new SimpleObjectProperty<>();

    @FXML
    private ListView<Collection> collectionListView;

    @FXML
    private TextField collectionNameField;

    @FXML
    private TextField serverField;

    @FXML
    private TextField titleField;

    @FXML
    private Label serverStatus;



    @Inject
    public CollectionEditCtrl(ServerUtils server, NoteEditCtrl noteEditCtrl, Config config, ConfigManager configManager, DialogUtil dialogUtil) {
        this.server = server;
        this.noteEditCtrl = noteEditCtrl;
        this.config = config;
	    this.configManager = configManager;
        this.dialogUtil = dialogUtil;
        this.currentCollection = null;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        collectionListView.setEditable(true);

        // Retrieve all collections from server and add them to listView
        List<Collection> collections = server.getCollections();

        // Create default collection if not present
        Collection defaultCollection = server.getDefaultCollection();
        if (!collections.contains(defaultCollection)) {
            collections.addFirst(defaultCollection); // Add default collection to the beginning or wherever you prefer
        }
        config.setDefaultCollection(defaultCollection); // For now, we're setting the config default as the server default
        saveConfig(config);

        collections.addAll(config.getCollections().stream().filter(x -> !collections.contains(x)).toList()); // add all config collections

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
                if (selectedCollection != null) {
                    if (selectedCollection.equals(defaultCollection)) {
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

                    if (newName.isBlank()) {
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
                    if (server.getCollections().stream()
                            .anyMatch(collection -> collection.getName().equals(newName)
                                    && !collection.equals(selectedCollection))) {
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

                    config.setCollectionName(selectedCollection, newName.strip());
                    saveConfig(config);
                    selectedCollection.setName(newName.strip());
                    server.addCollection(selectedCollection);
                    System.out.println("Collection title changed");
                    refresh();
                }
                return selectedCollection;
            }
        }));
        //listener for the ListView
        collectionListView.getSelectionModel().selectedItemProperty()
                        .addListener((_, _, current) -> handleSelectedCollection(current));
        //listener for the title change
        titleField.textProperty().addListener((_,_,text)-> statusListnerMethod(text));
        //change title on double click
        collectionListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int index = collectionListView.getSelectionModel().getSelectedIndex();
                if (index != -1) {
                    collectionListView.edit(index);
                }
            }
        });
        //on initialization no note is selected
        handleSelectedCollection(null);
    }

    private void statusListnerMethod(String text) {
        collectionNameField.setText(text); // TODO: Figure out if this should get updated with the note title
        if(server.getCollections()
                .stream()
                .anyMatch(collection -> collection.getName().equals(text))){
            serverStatus.setText("Collection already exists");
            return;

        }
        if(text.isBlank()) {
            serverStatus.setText("Blank Collection Title");
            return;
        }
        if(server.getCollections()
                .stream()
                .noneMatch(collection -> collection.getName().equals(text))) {
            serverStatus.setText("Collection can be created");
            return;
        }
        serverStatus.setText("Collection exists");
    }

    /**
     * Called by the listener inside initialize method whenever a collection is selected,
     * sets the fields on the right hand side accordingly to the collection selected or
     * if no collection is selected sets a default text in all fields
     * @param selectedCollection the collection that is selected
     */
    public void handleSelectedCollection(Collection selectedCollection) {
        if(selectedCollection == null) {
            //show a basic prompt if no collection selected
            titleField.setText("Select a collection to edit");
            serverField.setText("Select a collection to edit");
            collectionNameField.setText("Select a collection to edit");
            return;
        }
        //update the current collection
        currentCollection = selectedCollection;
        System.out.println("Collection " + selectedCollection.getName() + " selected");

        //update the fields accordingly
        titleField.setText(selectedCollection.getName());
        serverField.setText(server.getServerPath());
        collectionNameField.setText(selectedCollection.getName());
        serverStatus.setText("Collection exists");
    }

    public void changeCollectionTitle() {
        if(currentCollection == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No collection selected");
            alert.setContentText("Please select a collection first.");
            alert.showAndWait();
            handleSelectedCollection(null);
            return;
        }
        String newTitle = titleField.getText();
        if(newTitle.isBlank()){
            System.err.println("Collection name must not be empty.");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Collection name warning");
            alert.setHeaderText("Collection name cannot be empty");
            alert.setContentText("Please try to choose a proper collection title.");
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
            alert.getDialogPane();
            alert.showAndWait();
            return;
        }
        if(server.getCollections()
                .stream()
                .anyMatch(collection -> collection.getName().equals(newTitle))) {
            System.err.println("Collection name must be unique.");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Collection name warning");
            alert.setHeaderText("There is already a title with the given name");
            alert.setContentText("Please try to choose a unique collection title.");
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
            alert.getDialogPane();
            alert.showAndWait();
            return;
        }

        Collection modifiedCollection = currentCollection;
        modifiedCollection.setName(newTitle);
        server.addCollection(modifiedCollection);
        System.out.println("Collection title changed to " + newTitle);
        refresh();
    }

    public void changeCollectionServer() {
        if(currentCollection == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No collection selected");
            alert.setContentText("Please select a collection first.");
            alert.showAndWait();
            handleSelectedCollection(null);
        }
        String serverPath = serverField.getText();
        //TODO: CHECK IF THE SERVER PATH IS VALID ?
    }

    public void changeCollectionName() {
        if(currentCollection == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No collection selected");
            alert.setContentText("Please select a collection first.");
            alert.showAndWait();
            handleSelectedCollection(null);
        }
        //TODO: FIGURE OUT THE DIFFERENCE BETWEEN THIS AND THE TITLE FIELD
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

            config.addCollection(savedCollection); // add collection to config
            saveConfig(config);

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
    public void deleteCollection() {
        Collection selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
        if(selectedCollection != null) {
            try {
                server.deleteCollection(selectedCollection.getId());
                config.removeCollection(selectedCollection);
                saveConfig(config);
                collectionListView.getItems().remove(selectedCollection);
                noteEditCtrl.deleteCollectionToMenuButton(selectedCollection);
                refresh();
                System.out.println("Collection deleted successfully");
            } catch (Exception e) {
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
     * Method to save the config
     * @param config - config to save
     */
    public void saveConfig(Config config) {
        try {
            configManager.saveConfig(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to set the Language of the scene
     * @param locale the Locale with the language that needs to be set for the scene
     */
    public void setLanguage(Locale locale) {
        this.selectedLanguage.setValue(locale);
    }
}
