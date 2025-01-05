package client.scenes;

import client.utils.ServerUtils;
import commons.Collection;
import jakarta.inject.Inject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class CollectionEditCtrl implements Initializable {
    private final ServerUtils server;
    //private final MainCtrl mainCtrl;
    private final NoteEditCtrl noteEditCtrl;

    public final ObjectProperty<Locale> selectedLanguage = new SimpleObjectProperty<>();

    @FXML
    private ListView<Collection> collectionListView;

    @Inject
    public CollectionEditCtrl(ServerUtils server, NoteEditCtrl noteEditCtrl) {
        this.server = server;
        this.noteEditCtrl = noteEditCtrl;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        collectionListView.setEditable(true);

        // Retrieve all collections from server and add them to listView
        List<Collection> collections = server.getCollections();
        collectionListView.setItems(FXCollections.observableList(collections));

        // Create default collection if not present
        Collection defaultCollection = server.getDefaultCollection();
        if (!collections.contains(defaultCollection)) {
            collections.add(0, defaultCollection); // Add default collection to the beginning or wherever you prefer
        }

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
                        return selectedCollection;
                    }

                    //ensure titles are unique
                    boolean isUnique = true;
                    for(Collection collection: collections){
                        if(collection.getName().equals(newName.strip()) && !collection.equals(selectedCollection)){
                            isUnique = false;
                            break;
                        }
                    }
                    if (!isUnique) {
                        System.err.println("Collection name must be unique.");
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
            if(collectionName.isBlank())
                throw new IllegalArgumentException("Collection name cannot be blank.");

            List<Collection> existingCollections = server.getCollections();
            for(Collection collection: existingCollections){
                if(collection.getName().equals(collectionName.strip())){
                    throw new IllegalArgumentException("Collection name cannot be duplicated.");
                }
            }
            // Add collection to server
            Collection collection = new Collection(collectionName);
            Collection savedCollection = server.addCollection(collection);
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

        Collection collectionToFind = null;
        for (Collection collection : collections) {
            if (collection.getName().equalsIgnoreCase(name)) {
                collectionToFind = collection;
                break;
            }
        }
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
