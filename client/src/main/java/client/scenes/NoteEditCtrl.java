package client.scenes;

import client.utils.MarkdownUtil;
import client.utils.ServerUtils;
import commons.Note;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NoteEditCtrl implements Initializable {

    private final ServerUtils server;

    @FXML
    private ListView<Note> noteListView;

    @FXML
    private TextArea editingArea;

    @FXML
    private WebView markdownPreview;

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
        editingArea.textProperty().addListener((_, _, newText) -> {
            updateMarkdownView(newText);  // Pass the actual text content to update the WebView
        });
        // Until the user has selected a note to edit, display an informative message
        //  & do not allow the user to type.
        this.handleNoteSelect(null);
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

}