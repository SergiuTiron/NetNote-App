package client;

import com.fasterxml.jackson.annotation.JsonProperty;
import commons.Collection;
import commons.Note;

import java.util.ArrayList;
import java.util.List;

public class Config {
	private Collection defaultCollection;
	private final List<Collection> collections;
	private final List<Note> notes;

	public Config() {
		this.notes = new ArrayList<>();
		this.defaultCollection = null;
		this.collections = new ArrayList<>();
	}

	public void addNote(Note note) {
		notes.add(note);
	}

	public void removeNote(Note note) {
		if (notes.contains(note)){
			notes.remove(note);
		} else {
			throw new IllegalArgumentException("Collection not found");
		}
	}

	public List<Note> getNotes() {
		return notes;
	}

	public Collection getCollection(Collection collection) {
		if (collections.contains(collection)){
			return collection;
		} else {
			throw new IllegalArgumentException("No such collection exists");
		}
	}

	public Note getNote(Note note) {
		if (notes.contains(note)){
			return note;
		} else {
			throw new IllegalArgumentException("No such note exists");
		}
	}

	public Collection getDefaultCollection() {
		return defaultCollection;
	}

	@JsonProperty("defaultCollection")
	public void setDefaultCollection(Collection defaultCollection) {
		defaultCollection.setDefault(true);
		this.defaultCollection = defaultCollection;
	}

	public List<Collection> getCollections() {
		return collections;
	}

	public void addCollection(Collection collection) {
		this.collections.add(collection);
	}

	public void removeCollection(Collection collection) {
		if (collections.contains(collection)){
		collections.remove(collection);
		} else {
			throw new IllegalArgumentException("Collection not found");
		}
	}
}