package client.scenes;

import client.utils.MarkdownUtil;
import client.utils.ServerUtils;
import commons.Note;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class NoteEditCtrl implements Initializable {

    private final ServerUtils server;

    @FXML
    private ListView<Note> noteListView;

    @FXML
    private TextArea editingArea;

    @FXML
    private WebView markdownPreview;

    @FXML
    private TextField searchField;

    @Inject
    public NoteEditCtrl(ServerUtils server) {
        this.server = server;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        noteListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                this.setText(empty ? "" : "Note ID #" + note.id); // TODO: show note title instead
            }
        });
        noteListView.getSelectionModel().selectedItemProperty()
            .addListener((_, _, current) -> this.handleNoteSelect(current));
        editingArea.textProperty().addListener((_, _, newText) -> MarkdownUtil.renderMarkdownInWebView(newText, markdownPreview));
        // Until the user has selected a note to edit, display an informative message
        //  & do not allow the user to type.
        this.handleNoteSelect(null);
        keyShortcuts();
    }

    // Transformed this from if statements into a more complex and aesthetic lambda-based structure with the help of AI
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

    // Where all of the keycodes combinations are stored
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


    // Called whenever the WebView needs to be updated (because of writing in editingArea).
    private void updateMarkdownView(String markdownContent) {
        // Convert the content written in editingArea to HTML
        String htmlContent = MarkdownUtil.parseToHtml(markdownContent);
        markdownPreview.getEngine().loadContent(htmlContent);
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

    // Called whenever the user clicks the "Save Changes" button.
    // TODO: replace with automatic saving every x keystrokes & upon program exit.
    public void saveChanges() {
        Note note = noteListView.getSelectionModel().getSelectedItem();
        if (note == null)
            return;

        note.content = editingArea.getText();
        server.addNote(note);
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

        try {
            server.deleteNoteFromServer(selectedNote.getId());
            confirmationDelete(selectedNote);
            refresh();
        } catch (IOException e) {
            editingArea.setText("Failed to delete note. Please try again.");
            e.printStackTrace();
        }
    }

    private void confirmationDelete(Note selectedNote) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this note?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                noteListView.getItems().remove(selectedNote);
                clearFields();
            }
            else noteListView.getSelectionModel().clearSelection();
        });
    }

    private void clearFields() {
        noteListView.getSelectionModel().clearSelection();
        editingArea.setEditable(false);
        editingArea.setText("Select a note to start editing.");
    }

}