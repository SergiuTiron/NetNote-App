package client.scenes;

import client.utils.KeyStrokeUtil;
import client.utils.MarkdownUtil;
import client.utils.ServerUtils;
import commons.Note;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class NoteEditCtrl implements Initializable {

    private final ServerUtils server;
    private final KeyStrokeUtil keyStroke;

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
        noteListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                this.setText(empty ? "" : "Note ID #" + note.id); // TODO: show note title instead
            }
        });
        noteListView.getSelectionModel().selectedItemProperty()
            .addListener((_, _, current) -> this.handleNoteSelect(current));
        editingArea.textProperty().addListener((_, _, newText) ->
                MarkdownUtil.renderMarkdownInWebView(newText, markdownPreview));

        // TODO: create a new menu for editing the number of keystrokes for saving as for now this is hardcoded
        editingArea.setOnKeyTyped(event -> {
            keyStroke.increaseCounter();
            if(keyStroke.getCounter() == keyStroke.getTrigger() && !editingArea.getText().isEmpty()){
                autoSave();
                keyStroke.counterReset();
            }
        });
        // Until the user has selected a note to edit, display an informative message
        //  & do not allow the user to type.
        this.handleNoteSelect(null);

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

    public void autoSave() {
        Note note = noteListView.getSelectionModel().getSelectedItem();
        note.content = editingArea.getText();
        server.addNote(note);
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

}