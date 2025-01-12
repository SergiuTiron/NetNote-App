package client.scenes;

import client.Config;
import client.ConfigManager;
import client.utils.KeyStrokeUtil;
import client.utils.LocaleUtil;
import client.utils.MarkdownUtil;
import client.utils.ServerUtils;
import commons.Collection;
import commons.Note;
import jakarta.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class NoteEditCtrl implements Initializable {

    private final ServerUtils server;
    private final KeyStrokeUtil keyStroke;
    private final MarkdownUtil markdown;
    private final LocaleUtil localeUtil;
    private final MainCtrl mainCtrl;
    private ResourceBundle resourceBundle;
    private static boolean DELETE_FLAG;
    private Collection currentCollection;

    private final Config config;
    private final ConfigManager configManager;

    @FXML
    private Label saveLabel;

    @FXML
    private ListView<Note> noteListView;

    @FXML
    private TextArea editingArea;

    @FXML
    private WebView markdownPreview;

    @FXML
    private TextField searchField;

    @FXML
    private TextField titleField;

    @FXML
    private MenuButton collectionBox;

    @FXML
    private ComboBox<Locale> liveLanguageBox;

    @FXML
    private MenuButton currentCollectionDrop;

    public final ObjectProperty<Locale> selectedLanguage = new SimpleObjectProperty<>();
    private Note currentNote;

    @Inject
    public NoteEditCtrl(ServerUtils server, KeyStrokeUtil keyStroke, MarkdownUtil markdown, LocaleUtil localeUtil, MainCtrl mainCtrl, Config config, ConfigManager configManager) {
        this.server = server;
        this.keyStroke = keyStroke;
        this.markdown = markdown;
        this.localeUtil = localeUtil;
        this.mainCtrl = mainCtrl;
	    this.config = config;
	    this.configManager = configManager;
	    DELETE_FLAG = false;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        // Load collections from the server
        List<Collection> collections = server.getCollections();
        // Add the collections as menuItems
        for (Collection collection : collections) {
            MenuItem collectionItem = new MenuItem(collection.getName());
            collectionItem.setOnAction(event -> {
                handleSpecificCollectionSelected(collection);
            });

            collectionBox.getItems().add(collectionItem);
        }
        for (Collection collection : config.getCollections()) {
            collections.add(collection);
            addCollectionToMenuButton(collection);
        }
        // Set the "All" option as default selection
        collectionBox.setText("All");

        for (Collection collection : collections) {
            MenuItem collectionChangeItem = new MenuItem(collection.getName());
            collectionChangeItem.setOnAction(event -> {
                moveNoteToCollection(currentNote, collectionChangeItem);
            });

            currentCollectionDrop.getItems().add(collectionChangeItem);
        }

        liveLanguageBox.setItems(FXCollections.observableList(localeUtil.getAvailableLocales()));
        liveLanguageBox.setCellFactory(_ -> new ListCell<>() {
            private final ImageView flagView = new ImageView();

            {
                flagView.setFitHeight(20);
                flagView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Locale locale, boolean empty) {
                super.updateItem(locale, empty);
                if (empty) {
                    this.setGraphic(null);
                    this.setText(null);
                } else {
                    flagView.setImage(localeUtil.getFlagImage(locale));
                    this.setGraphic(this.flagView);
                    this.setText(locale.getDisplayName(selectedLanguage.get()));
                }
            }
        });
        liveLanguageBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Locale locale, boolean empty) {
                super.updateItem(locale, empty);
                this.setText(!empty ? locale.getDisplayName(selectedLanguage.get()) : null);
            }
        });
        liveLanguageBox.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue == null) {
                return;
            }
            this.setLanguage(newValue);
        });

        noteListView.setCellFactory(_ -> new TextFieldListCell<>(new StringConverter<>() {
            @Override
            public String toString(Note note) {
                if (note == null) return "";
                return note.getTitle();
            }

            @Override
            public Note fromString(String newTitle) {
                Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
                if (selectedNote.getTitle().equals(newTitle.strip())) {
                    System.out.println("Title is unchanged. No action taken.");
                    return selectedNote;
                }
                Optional<Note> duplicatedTitle = server.getNotes()
                        .stream()
                        .filter(note -> note.getTitle().equals(newTitle.strip()))
                        .findAny();
                if (selectedNote != null && duplicatedTitle.isEmpty()) {
                    selectedNote.setTitle(newTitle.strip());
                    titleField.setText(newTitle.strip());
                    server.updateNote(selectedNote);
                    config.getNote(selectedNote).setTitle(newTitle);
                    saveConfig(config);
                } else if (duplicatedTitle.isPresent()) {
                    System.out.println("Title already exists");
                    Alert alert = new Alert(Alert.AlertType.WARNING); // Alert type
                    Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
                    alert.setTitle("Title Warnings");
                    alert.setHeaderText("This note title already exists");
                    alert.setContentText("Please enter a unique note title");

                    alert.showAndWait();
                }
                return selectedNote;
            }
        }));

        titleField.setEditable(false);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterNotes();
        });

        noteListView.setEditable(true);
        //double-click triggers note editing
        noteListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int selectedNoteIndex = noteListView.getSelectionModel().getSelectedIndex();
                if (selectedNoteIndex != -1)
                    noteListView.edit(selectedNoteIndex);
            }
        });

        noteListView.getSelectionModel().selectedItemProperty()
                .addListener((_, old, current) -> {
                    if (DELETE_FLAG) {
                        DELETE_FLAG = false;
                        if (current != null) {
                            titleField.setText(current.getTitle());
                        }
                        return;
                    }
                    if (current == null || current.getTitle().isEmpty()) {
                        return;
                    } else {
                        titleField.setText(current.getTitle());
                    }
                    this.saveChanges(old);
                    this.handleNoteSelect(current);
                });

        editingArea.textProperty().addListener((_, _, newText) ->
                markdown.renderMarkdownInWebView(newText, markdownPreview));

        editingArea.setOnKeyTyped(event -> {
            keyStroke.increaseCounter();
            if (keyStroke.getCounter() == keyStroke.getTrigger() && !editingArea.getText().isEmpty()) {
                autoSave();
                keyStroke.counterReset();
            }
        });
        editingArea.textProperty().addListener((_, _, newText) -> renderMarkdown(newText));
        // Until the user has selected a note to edit, display an informative message
        //  & do not allow the user to type.
        this.handleNoteSelect(null);
        keyShortcuts();

    }

    /**
     * Handler for "All" option
     */
    public void handleAllCollectionsSelected() {
        System.out.println("All button pressed");
        collectionBox.setText("All");

        List<Note> notes = server.getNotes();
        currentCollection = server.getDefaultCollection();

        // Clear the current list
        noteListView.getItems().clear();

        // Add the notes to the ListView
        noteListView.getItems().addAll(notes);
        noteListView.getItems().addAll(config.getNotes());
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
     * Handler for "Edit collections"
     */
    public void handleEditCollections() {
        System.out.println("Edit button pressed");
        mainCtrl.showCollectionEdit();
    }

    /**
     * A method to add a button for a new collection to the collectionBox (MenuButton)
     *
     * @param collection - collection to add
     */
    public void addCollectionToMenuButton(Collection collection) {
        System.out.println("Collection button added"); //for debugging purposes
        MenuItem newCollectionItem = new MenuItem(collection.getName());
        MenuItem newCollectionChangeItem = new MenuItem(collection.getName());

        newCollectionItem.setOnAction(event -> {
            handleSpecificCollectionSelected(collection);
        });

        newCollectionChangeItem.setOnAction(event -> {
            moveNoteToCollection(currentNote, newCollectionChangeItem);
        });

        // Add the new MenuItem to the MenuButton
        collectionBox.getItems().add(newCollectionItem);
        currentCollectionDrop.getItems().add(newCollectionChangeItem);
    }

    /**
     * A method to delete a button for a deleted collection from the collectionBox (MenuButton)
     *
     * @param selectedCollection - collection to find the button that needs to be deleted
     */
    public void deleteCollectionToMenuButton(Collection selectedCollection) {
        System.out.println("Collection button deleted");
        MenuItem collectionsButton = collectionBox.getItems()
                .stream()
                .filter(x -> x.getText().equals(selectedCollection.getName()))
                .findFirst()
                .get();
        MenuItem collectionChangeButton = currentCollectionDrop.getItems()
                .stream()
                .filter(x -> x.getText().equals(selectedCollection.getName()))
                .findFirst()
                .get();
        collectionBox.getItems().remove(collectionsButton);
        currentCollectionDrop.getItems().remove(collectionChangeButton);
    }

    /**
     * Displaying a given list of notes (from a collection) in the listview
     *
     * @param selectedItem - collection
     */
    private void handleSpecificCollectionSelected(Collection selectedItem) {
        System.out.println("Collection handled"); //for debugging purposes
        List<Note> notes = server.getNotesByCollection(selectedItem.getId());
        currentCollection = selectedItem;
        currentNote = null;
        collectionBox.setText(selectedItem.getName()); //set the name of the collection to show in the MenuButton
        clearFields();
        // Clear the current list
        noteListView.getItems().clear();

        // Add the notes to the ListView
        noteListView.getItems().addAll(notes);
    }

    // Method to render markdown
    private void renderMarkdown(String markdownContent) {
        URL cssFileUrl = MarkdownUtil.class.getResource("/css/markdown-style.css");
        if (cssFileUrl != null) {
            markdown.renderMarkdownInWebView(markdownContent, markdownPreview);
        } else {
            markdown.renderMarkdown(markdownContent, markdownPreview);
        }
    }

    private void saveLabelTransition() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), saveLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            // Hold the label visible for 1 seconds
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), saveLabel);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.seconds(1)); // Wait 1 second before fading out
            fadeOut.play();
        });
        fadeIn.play();
    }

    public void changeNoteSavingSettings(ActionEvent event) {
        //System.out.println("something"); solely for debugging
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle(resourceBundle.getString("popup.autosave.title"));
        alert.setHeaderText(resourceBundle.getString("popup.autosave.text")
                .replace("%num%", String.valueOf(keyStroke.getTrigger())));
        alert.getDialogPane().getScene().getWindow().setWidth(400);
        alert.getDialogPane().getScene().getWindow().setHeight(200);

        TextField textField = new TextField();
        textField.setPromptText(resourceBundle.getString("popup.autosave.prompt"));
        // Add the TextField to a layout (VBox)
        VBox content = new VBox();
        content.setSpacing(10);
        content.getChildren().add(textField);

        // Set the custom content to the Alert
        alert.getDialogPane().setContent(content);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && !textField.getText().isEmpty()) {
                try {
                    keyStroke.setTriggerCount(Integer.parseInt(textField.getText()));
                } catch (NumberFormatException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle(resourceBundle.getString("popup.autosave.invalid.title"));
                    error.setHeaderText(resourceBundle.getString("popup.autosave.invalid.text"));
                    error.showAndWait();
                }
            } else if (response == ButtonType.CANCEL) {
                alert.close();
            }
        });

    }

    private void keyShortcuts() {
        noteListView.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    Map<KeyCombination, Runnable> keyActions = keyCodeCombinations();
                    keyActions.entrySet().stream()
                            .filter(entry -> entry.getKey().match(event))
                            .findFirst()
                            .ifPresent(entry -> entry.getValue().run());
                });
            }
        });
    }

    // Where all the keycodes combinations are stored
    private Map<KeyCombination, Runnable> keyCodeCombinations() {
        return Map.of(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::createNewNote,
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), this::saveChanges,
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), this::refresh,
                new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), () -> {
                    try {
                        deleteButton();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    // Called whenever the user clicks on one of the notes in the sidebar.
    private void handleNoteSelect(Note note) {
        if (note == null) {
            // If no note is selected, disable editing and show a default message
            editingArea.setEditable(false);
            editingArea.setText(resourceBundle.getString("initialText"));
            return;
        }
        // If a note is selected, enable editing and display its content
        currentNote = note;
        editingArea.setEditable(true);
        editingArea.setText(note.getContent());
        currentCollectionDrop.setText(note.getCollection().getName());
    }

    // Called whenever the user clicks the "New Note" button.
    public void createNewNote() {
        Note note = server.newEmptyNote();
        titleField.setText(note.getTitle());
        Collection defaultCollection = server.getDefaultCollection();
        if (currentCollection == null || currentCollection == defaultCollection) {
            note.setCollection(defaultCollection);
            server.linkNoteToCollection(defaultCollection.getId(), note);
            currentCollectionDrop.setText(defaultCollection.getName());
        } else {
            note.setCollection(currentCollection);
            server.linkNoteToCollection(currentCollection.getId(), note);
        }
        config.addNote(note);
        saveConfig(config);
        noteListView.getItems().add(note);
        // Updates the location of the editing area on the note currently created
        noteListView.getSelectionModel().select(note);
        editingArea.setEditable(true);
    }

    // Called whenever the user clicks the "Refresh" button.
    public void refresh() {
        List<Note> notes;
        if (currentCollection == null && collectionBox.getText().equals("All")) {
            notes = server.getNotes();
        } else {
            notes = server.getNotesByCollection(currentCollection.getId());
        }
        noteListView.setItems(FXCollections.observableList(notes));
    }

    public void autoSave() {
        Note note = noteListView.getSelectionModel().getSelectedItem();
        note.setContent(editingArea.getText());
        server.addNote(note);
        saveLabelTransition();
        System.out.println("Changes saved");// This line is just for debugging purpose
    }

    /**
     * Called on exiting the app
     */
    public void saveChanges() {
        Note note = noteListView.getSelectionModel().getSelectedItem();
        if (note == null)
            return;
        note.setContent(editingArea.getText());
        server.addNote(note);
        saveLabelTransition();
        System.out.println("Changes were saved.");
    }

    /**
     * Called when switching notes in ListView and when exiting the app
     */
    public void saveChanges(Note note) {
        if (note == null)
            return;
        note.setContent(editingArea.getText());
        server.addNote(note);
        saveLabelTransition();
        System.out.println("Changes were saved.");
    }

    /**
     *     called when the user clicks the "Search" button
     *     This method displays all the notes in the current collection that contain the given keyword.
     */
    public void filterNotes() {
        String query = searchField.getText();
        if(query == null || query.isEmpty()){
            if(currentCollection != null){
                handleSpecificCollectionSelected(currentCollection);
            }
            else{
                handleAllCollectionsSelected();
            }
            return;
        }
        List<Note> filteredNotes;
        if (currentCollection == null) {
            filteredNotes = server.searchKeyword(query, null);
        } else {
            filteredNotes = server.searchKeyword(query, currentCollection.getId());
        }
        noteListView.setItems(FXCollections.observableList(filteredNotes));
    }

    /**
     * Called whenever the user clicks the "Delete" button.
     */
    public void deleteButton() throws IOException {
        Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
        if (selectedNote == null) {
            editingArea.setText("Select a note to delete.");
            return;
        }
        boolean deleteConfirmed = confirmationDelete(selectedNote);
        if (!deleteConfirmed) {
            return;
        }
        try {
            DELETE_FLAG = true;
            server.deleteNoteFromServer(selectedNote.getId());
            config.removeNote(selectedNote);
            saveConfig(config);
            noteListView.getItems().remove(selectedNote);
            clearFields();
            refresh();
        } catch (IOException e) {
            editingArea.setText("Failed to delete note. Please try again.");
            e.printStackTrace();
        }
    }

    private boolean confirmationDelete(Note selectedNote) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, resourceBundle.getString("popup.confirmDelete"));
        Optional<ButtonType> response = alert.showAndWait();
        return response.isPresent() && response.get() == ButtonType.OK;
    }

    private void clearFields() {
        noteListView.getSelectionModel().clearSelection();
        editingArea.setEditable(false);
        editingArea.setText("Select a note to start editing");
        titleField.setText("Select a note to start editing");
        currentCollectionDrop.setText("ChangeCollection");
    }

    public void setLanguage(Locale locale) {
        this.selectedLanguage.setValue(locale);
        liveLanguageBox.setValue(locale);
    }

    public void moveNoteToCollection(Note currentNote, MenuItem collectionChangeItem) {
        System.out.println("Collection trying to be moved");
        if (currentNote == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("There is no current note selected");
            alert.setContentText("Please select a note when trying to move to another collection");
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
            alert.getDialogPane();
            alert.showAndWait();
            return;
        }
        try {
            Collection newCollection = server.getCollectionByName(collectionChangeItem.getText());
            if (currentNote.getCollection().equals(newCollection)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("The note is trying to be moved to the same collection");
                alert.setContentText("Please select a different collection when moving this note");
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
                alert.getDialogPane();
                alert.showAndWait();
                return;
            }
            currentNote.setCollection(newCollection);
            server.updateNote(currentNote);
            Alert info = new Alert(Alert.AlertType.INFORMATION, "Note successfully moved to " + newCollection.getName() + ".");
            Stage alertStage = (Stage) info.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
            info.getDialogPane();
            info.showAndWait();
            refresh();
            clearFields();
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR, "Failed to move the note. Please try again.");
            Stage alertStage = (Stage) error.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image("appIcon/NoteIcon.jpg"));
            error.getDialogPane();
            error.showAndWait();
        }
    }

    @FXML
    private void onSelectLanguage(ActionEvent event) {
        List<Locale> availableLocales = Arrays.asList(Locale.getAvailableLocales());
        List<String> languages = availableLocales.stream()
                .map(Locale::getDisplayName)
                .collect(Collectors.toList());

        ChoiceDialog<String> dialog = new ChoiceDialog<>("English", languages);
        dialog.setTitle("Select Language");
        dialog.setHeaderText("Choose Your Preferred Language");
        dialog.setContentText("Language:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(selectedLanguage -> {
            System.out.println("Saved Language: " + selectedLanguage);
            // TODO: Save the selected language in the database
            // savePreferredLanguage(selectedLanguage);

            Locale newLocale = mapLanguageToLocale(selectedLanguage);
            setLanguage(newLocale);

        });
    }

    private Locale mapLanguageToLocale(String language) {
        return switch (language) {
            case "Dutch" -> new Locale("nl");
            case "Romanian" -> new Locale("ro");
            case "Bulgarian" -> new Locale("bg");
            case "Italian" -> new Locale("it");
            default -> Locale.ENGLISH;
        };
    }

    public Note getCurrentNote() {
        return currentNote;
    }

    public void setCurrentNote(Note note) {
        noteListView.getSelectionModel().select(note);
    }

}