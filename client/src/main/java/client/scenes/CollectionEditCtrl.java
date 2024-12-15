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
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CollectionEditCtrl implements Initializable {
    private final ServerUtils server;
    //private final MainCtrl mainCtrl;

    public final ObjectProperty<Locale> selectedLanguage = new SimpleObjectProperty<>();

    @FXML
    private ListView<Collection> collectionListView;

    @Inject
    public CollectionEditCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
    //   this.mainCtrl = mainCtrl;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
                return selectedCollection;
            }
        }));
    }

    public void createCollection() {
        Collection collection = server.newEmptyCollection();
        collectionListView.getItems().add(collection);
        collectionListView.getSelectionModel().select(collection);
        System.out.println("Collection created successfully");
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
