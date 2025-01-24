package client.scenes;

import client.Config;
import client.ConfigManager;
import client.utils.DialogUtil;
import client.utils.ServerUtils;
import commons.Collection;
import jakarta.inject.Inject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class CollectionEditCtrl implements Initializable {
    // Utils fields
    private final ServerUtils server;
    private final DialogUtil dialogUtil;
    // Controller fields
    private final NoteEditCtrl noteEditCtrl;
    // Config fields
    private final Config config;
    private final ConfigManager configManager;
    // Flag fields
    private Collection currentCollection;
    // Language fields
    private ResourceBundle resourceBundle;
    public final ObjectProperty<Locale> selectedLanguage = new SimpleObjectProperty<>();

    @FXML
    private ListView<Collection> collectionListView;

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
                    if (newName.isBlank()) {
                        dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING,
                                "popup.collections.emptyName");
                        return selectedCollection;
                    }

                    //ensure titles are unique
                    if (server.getCollections().stream()
                            .anyMatch(collection -> collection.getName().equals(newName)
                                    && !collection.equals(selectedCollection))) {
                        System.err.println("Collection name must be unique.");
                        dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING,
                                "popup.collections.duplicateName");
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
        // Listener for the ListView
        collectionListView.getSelectionModel().selectedItemProperty()
                .addListener((_, _, current) -> handleSelectedCollection(current));
        // Listener for the title change
        titleField.textProperty().addListener((_, _, text) -> statusListenerMethod(text));
        // Listener for the server change
        serverField.textProperty().addListener((_, _, text) -> {
            if(serverField.isFocused())
                serverListenerMethod(text);
        });
        // Change title on double click
        collectionListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int index = collectionListView.getSelectionModel().getSelectedIndex();
                if (index != -1) {
                    collectionListView.edit(index);
                }
            }
        });
        // On initialization no note is selected
        handleSelectedCollection(null);
    }

    // COLLECTION HANDLING

    /**
     * Method for creating a new collection based on user input
     */
    public void createCollection() {
        // Create a TextInputDialog for entering the collection name
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(resourceBundle.getString("popup.collections.new.title"));
        dialog.setHeaderText(resourceBundle.getString("popup.collections.new.header"));
        dialog.setContentText(resourceBundle.getString("popup.collections.new.input"));

        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));

        // Show the dialog and wait for a response
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(collectionName -> {
            if (collectionName.isBlank()) {
                dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                        "popup.collections.emptyName");
                return;
            }

            List<Collection> existingCollections = server.getCollections();
            if (existingCollections
                    .stream()
                    .anyMatch(collection -> collection.getName().equals(collectionName.strip()))) {
                dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                        "popup.collections.duplicateName");
                return;
            }
            // Add collection to server
            Collection collection = new Collection(collectionName);
            Collection savedCollection = server.addCollection(collection);

            config.addCollection(savedCollection); // add collection to config
            config.setDefaultCollection(configManager.getDefaultCollection()); // make sure the default collection is not replaced
            saveConfig(config);

            // Add collection to listView
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.INFORMATION,
                    "popup.collections.createdSuccessfully");
            collectionListView.getItems().add(savedCollection);
            collectionListView.getSelectionModel().select(savedCollection);
            // Add collection to MenuButton
            this.refresh();
            System.out.println("Collection created successfully");
        });


    }

    /**
     * Delete selected collection
     */
    public void deleteCollection() {
        Collection selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
        if (selectedCollection == null) {
            dialogUtil.showDialog(resourceBundle, Alert.AlertType.WARNING, "popup.Collection.delete.noteSelected");
            System.err.println("Delete attempt with no collection selected");
            return;
        }
        if (confirmationDelete(selectedCollection)) {
            try {
                server.deleteCollection(selectedCollection.getId());
                config.removeCollection(selectedCollection);
                saveConfig(config);
                collectionListView.getItems().remove(selectedCollection);
                this.refresh();
                System.out.println("Collection deleted successfully");
                dialogUtil.showDialog(resourceBundle, Alert.AlertType.INFORMATION, "popup.Collection.delete.successfully");
            } catch (Exception e) {
                System.err.println("Failed to delete collection");
                e.printStackTrace();
            }
        }
    }

    private boolean confirmationDelete(Collection selectedCollection) {
        Optional<ButtonType> response = dialogUtil.showDialog(resourceBundle, Alert.AlertType.CONFIRMATION, "popup.collection.confirmDelete");
        return response.isPresent() && response.get() == ButtonType.OK;
    }

    public void refresh() {
        noteEditCtrl.deleteAllButtons();
        List<Collection> collections = server.getCollections();
        System.out.println(collections.toString());
        for (Collection collection : collections) {
            noteEditCtrl.addCollectionToMenuButton(collection, configManager.getDefaultCollection().equals(collection));

        }
        collectionListView.setItems(FXCollections.observableList(collections));
        System.out.println("Collections refreshed");
    }

    // UI FIELDS UPDATES

    private void serverListenerMethod(String serverURL) {
        String regex = "^(http://)(localhost(:\\d+)?|[\\w.-]+(\\.[a-z]{2,})+)(/.*)?$";
        if (currentCollection == null) {
            return;
        }
        System.out.println(serverURL);
        if(!serverURL.matches(regex)) {
            serverStatus.setText(this.resourceBundle.getString("labels.collections.status.invalidPath"));
        } else {
            serverCheckConnection(serverURL);
        }
    }

    private void serverCheckConnection(String serverURL) {
        System.out.println("Request made to: " + serverURL);
        if(server.makeRequest(serverURL, currentCollection) == 200) {
            this.statusListenerMethod(currentCollection.getName());
        } else {
            serverStatus.setText(this.resourceBundle.getString("labels.collections.status.cannotConnect"));
        }
    }

    private void statusListenerMethod(String text) {

        if (server.getCollections()
                .stream()
                .anyMatch(collection -> collection.getName().equals(text))) {
            serverStatus.setText(this.resourceBundle.getString("labels.collections.status.alreadyExists"));
            return;

        }
        if (text.isBlank()) {
            serverStatus.setText(this.resourceBundle.getString("labels.collections.status.blankTitle"));
            return;
        }
        if (server.getCollections()
                .stream()
                .noneMatch(collection -> collection.getName().equals(text))) {
            serverStatus.setText(this.resourceBundle.getString("labels.collections.status.canBeCreated"));
            return;
        }
        serverStatus.setText(this.resourceBundle.getString("labels.collections.status.exists"));
    }

    /**
     * Called by the listener inside initialize method whenever a collection is selected,
     * sets the fields on the right hand side accordingly to the collection selected or
     * if no collection is selected sets a default text in all fields
     *
     * @param selectedCollection the collection that is selected
     */
    public void handleSelectedCollection(Collection selectedCollection) {
        if (selectedCollection == null) {
            //show a basic prompt if no collection selected
            titleField.setEditable(false);
            serverField.setEditable(false);

            String initialText = this.resourceBundle.getString("labels.collections.initialText");
            titleField.setText(initialText);
            serverField.setText(initialText);
            return;
        }

        //update the current collection
        currentCollection = selectedCollection;
        System.out.println("Collection " + selectedCollection.getName() + " selected");

        //update the fields accordingly
        titleField.setEditable(true);
        serverField.setEditable(true);

        titleField.setText(selectedCollection.getName());
        serverField.setText(server.getServerPath());
        serverStatus.setText(this.resourceBundle.getString("labels.collections.status.exists"));
    }

    public void changeCollectionTitle() {
        if (currentCollection == null) {
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.INFORMATION,
                    "popup.collections.noneSelected");
            handleSelectedCollection(null);
            return;
        }
        String newTitle = titleField.getText();
        if (newTitle.isBlank()) {
            System.err.println("Collection name must not be empty.");
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                    "popup.collections.emptyName");
            return;
        }
        if (server.getCollections()
                .stream()
                .anyMatch(collection -> collection.getName().equals(newTitle))) {
            System.err.println("Collection name must be unique.");
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.WARNING,
                    "popup.collections.duplicateName");
            return;
        }

        Collection modifiedCollection = currentCollection;
        config.setCollectionName(currentCollection, newTitle.strip());
        saveConfig(config);
        modifiedCollection.setName(newTitle.strip());
        server.addCollection(modifiedCollection);

        System.out.println("Collection title changed to " + newTitle);
        this.refresh();
    }

    public void changeCollectionServer() {
        if(serverStatus.getText().equals(resourceBundle.getString("labels.collections.status.cannotConnect"))){
            dialogUtil.showDialog(resourceBundle, Alert.AlertType.ERROR,"popup.collections.serverUnavailable");
            return;
        } else if(serverStatus.getText().equals(resourceBundle.getString("labels.collections.status.invalidPath"))){
            dialogUtil.showDialog(resourceBundle, Alert.AlertType.ERROR,"popup.collections.invalidPath");
            return;
        } else if(serverStatus.getText().equals(resourceBundle.getString("labels.collections.status.alreadyExists"))){
            dialogUtil.showDialog(resourceBundle, Alert.AlertType.ERROR,"popup.collections.alreadyExistsServer");
            return;
        }
        collectionListView.requestFocus();
    }

    // LANGUAGE

    /**
     * Method to set the Language of the scene
     *
     * @param locale the Locale with the language that needs to be set for the scene
     */
    public void setLanguage(Locale locale) {
        this.selectedLanguage.setValue(locale);
    }

    /**
     * method triggered when pressing the "Make Default Button"
     * it sets the selected method as default and displays an informative message
     */
    public void setCollectionAsDefault() {
        Collection selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
        if (selectedCollection == null) {
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.INFORMATION,
                    "popup.collections.noneSelected");
        } else {
            noteEditCtrl.updateButtons(configManager.getDefaultCollection(), configManager.getDefaultCollection().getName());
            configManager.setDefaultCollection(selectedCollection);
            dialogUtil.showDialog(this.resourceBundle, Alert.AlertType.INFORMATION,
                    "popup.collections.defaultChanged",
                    Map.of("%name%", selectedCollection.getName()));
            noteEditCtrl.updateButtons(selectedCollection, selectedCollection.getName() + "(Default)");
        }
    }

    // CONFIG

    /**
     * Method to save the config
     *
     * @param config - config to save
     */
    public void saveConfig(Config config) {
        try {
            configManager.saveConfig(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
