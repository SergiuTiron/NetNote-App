package client;

import commons.Collection;

import java.util.ArrayList;
import java.util.List;

public class Config {
	private Collection defaultCollection;
	private final List<Collection> collections;

	public Config() {
		this.defaultCollection = null;
		this.collections = new ArrayList<>();
	}

	// Getters and setters for your fields
	public Collection getDefaultCollection() {
		return defaultCollection;
	}

	public void setDefaultCollection(Collection defaultCollection) {
		this.defaultCollection = defaultCollection;
	}

	public List<Collection> getCollections() {
		return collections;
	}

	public void addCollection(Collection collection) {
		this.collections.add(collection);
	}

	public void removeCollection(Collection collection) {
		collections.remove(collection);
		throw new IllegalArgumentException("Collection not found");
	}
}