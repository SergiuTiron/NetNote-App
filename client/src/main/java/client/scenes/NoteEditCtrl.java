package client.scenes;

import client.utils.KeyStrokeUtil;
import client.utils.MarkdownUtil;
import client.utils.ServerUtils;
import commons.Note;
import jakarta.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class NoteEditCtrl implements Initializable {

    private final ServerUtils server;
    private final KeyStrokeUtil keyStroke;

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

    @Inject
    public NoteEditCtrl(ServerUtils server, KeyStrokeUtil keyStroke) {
        this.server = server;
        this.keyStroke = keyStroke;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        noteListView.setCellFactory(_ -> new TextFieldListCell<>(new StringConverter<>(){
            @Override
            public String toString(Note note) {
                if (note == null) return "";
                return note.getTitle();
            }
            @Override
            public Note fromString(String newTitle) {
                Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
                if (selectedNote != null) {
                    selectedNote.setTitle(newTitle);
                    server.updateNote(selectedNote);
                }
                return selectedNote;
            }
        }));

        noteListView.setEditable(true);

        //double-click triggers note editing
        noteListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int selectedNoteIndex = noteListView.getSelectionModel().getSelectedIndex();
                if(selectedNoteIndex != -1)
                    noteListView.edit(selectedNoteIndex);
            }
        });

        noteListView.getSelectionModel().selectedItemProperty()
            .addListener((_, _, current) -> this.handleNoteSelect(current));
        editingArea.textProperty().addListener((_, _, newText) ->
                MarkdownUtil.renderMarkdownInWebView(newText, markdownPreview));

        editingArea.setOnKeyTyped(event -> {
            keyStroke.increaseCounter();
            if(keyStroke.getCounter() == keyStroke.getTrigger() && !editingArea.getText().isEmpty()){
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

    // Method to render markdown
    private void renderMarkdown(String markdownContent) {
        URL cssFileUrl = MarkdownUtil.class.getResource("/css/markdown-style.css");
        if (cssFileUrl != null) {
            MarkdownUtil.renderMarkdownInWebView(markdownContent, markdownPreview);
        } else {
            MarkdownUtil.renderMarkdown(markdownContent, markdownPreview);
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

        alert.setTitle("AutoSave Settings");
        alert.setHeaderText("The current number of keystrokes for saving is: " + keyStroke.getTrigger() + " keystrokes.");
        alert.getDialogPane().getScene().getWindow().setWidth(400);
        alert.getDialogPane().getScene().getWindow().setHeight(200);

        TextField textField = new TextField();
        textField.setPromptText("Enter the number of keystrokes to save upon");
        // Add the TextField to a layout (VBox)
        VBox content = new VBox();
        content.setSpacing(10);
        content.getChildren().add(textField);

        // Set the custom content to the Alert
        alert.getDialogPane().setContent(content);

        alert.showAndWait().ifPresent(response -> {
            if(response == ButtonType.OK && !textField.getText().isEmpty()){
                try{
                    keyStroke.setTriggerCount(Integer.parseInt(textField.getText()));
                }catch (NumberFormatException e){
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Invalid number");
                    error.setHeaderText("The number of keystrokes you provided is invalid.");
                    error.showAndWait();
                }
            }
            else if(response == ButtonType.CANCEL){
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
            editingArea.setText("Select a note to start editing.");
            return;
        }

        // If a note is selected, enable editing and display its content
        editingArea.setEditable(true);
        editingArea.setText(note.content);
    }

    // Called whenever the user clicks the "New Note" button.
    public void createNewNote() {
        Note note = server.newEmptyNote();
        noteListView.getItems().add(note);
        // Updates the location of the editing area on the note  currently created
        noteListView.getSelectionModel().select(note);
        editingArea.setEditable(true);
    }

    // Called whenever the user clicks the "Refresh" button.
    public void refresh() {
        List<Note> notes = server.getNotes();
        noteListView.setItems(FXCollections.observableList(notes));
    }

    public  void autoSave() {
        Note note = noteListView.getSelectionModel().getSelectedItem();
        note.content = editingArea.getText();
        server.addNote(note);
        saveLabelTransition();
        //System.out.println("Changes saved"); This line is just for debugging purpose
    }

    // Called whenever the user clicks the "Save Changes" button.
    // TODO: make a save on exit as well
    public void saveChanges() {
        Note note = noteListView.getSelectionModel().getSelectedItem();
        if (note == null)
            return;
        note.content = editingArea.getText();
        server.addNote(note);
        saveLabelTransition();
    }

    //called when the user clicks the "Search" button
    public void filterNotes() {
        String query = searchField.getText();
        if(query == null || query.isEmpty()) {
            noteListView.setItems(FXCollections.observableList(server.getNotes()));
            return;
        }
        List<Note> filteredNotes = new ArrayList<>();
        for(Note note : server.getNotes())
            if(note.content.toLowerCase().contains(query.toLowerCase()))
                filteredNotes.add(note);

        noteListView.setItems(FXCollections.observableList(filteredNotes));
    }

    // Called whenever the user clicks the "Delete" button.
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
            server.deleteNoteFromServer(selectedNote.getId());
            noteListView.getItems().remove(selectedNote);
            clearFields();
            refresh();
        } catch (IOException e) {
            editingArea.setText("Failed to delete note. Please try again.");
            e.printStackTrace();
        }
    }

    private boolean confirmationDelete(Note selectedNote) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this note?");
        Optional<ButtonType> response = alert.showAndWait();
        return response.isPresent() && response.get() == ButtonType.OK;
    }

    private void clearFields() {
        noteListView.getSelectionModel().clearSelection();
        editingArea.setEditable(false);
        editingArea.setText("Select a note to start editing.");
    }

}