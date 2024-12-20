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
import org.checkerframework.checker.units.qual.C;

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
    public CollectionEditCtrl(ServerUtils server, MainCtrl mainCtrl, NoteEditCtrl noteEditCtrl) {
        this.server = server;
//        this.mainCtrl = mainCtrl;
        this.noteEditCtrl = noteEditCtrl;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        collectionListView.setEditable(false);

        List<Collection> collections = server.getCollections();
        collectionListView.setItems(FXCollections.observableList(collections));

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
            public Collection fromString(String newTitle) {
                Collection selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
                if (selectedCollection != null) {
                    selectedCollection.setName(newTitle);
                }
                Collection defaultCollection = server.getDefaultCollection();
                return selectedCollection;
            }
        }));
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
            Collection collection = new Collection(collectionName);
            server.addCollection(collection);
            collectionListView.getItems().add(collection);
            collectionListView.getSelectionModel().select(collection);
            noteEditCtrl.addCollectionToMenuButton(collection);
            System.out.println("Collection created successfully");
        });
    }

    /**
     * Delete selected collection
     */
    public void deleteCollection() throws IOException {
        Collection selectedCollection = collectionListView.getSelectionModel().getSelectedItem();
        try {
            server.deleteCollection(selectedCollection.getId());
            collectionListView.getItems().remove(selectedCollection);
            refresh();
        } catch (IOException e) {
            System.err.println("Failed to delete collection");
            e.printStackTrace();
        }
        System.out.println("Collection deleted successfully");
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
