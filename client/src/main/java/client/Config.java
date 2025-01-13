package client;

import com.fasterxml.jackson.annotation.JsonProperty;
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

	public Collection getCollection(Collection collection) {
		if (collections.contains(collection)){
			return collection;
		} else {
			throw new IllegalArgumentException("No such collection exists");
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

	public void setCollectionName(Collection collection, String newName) {

		if (collections.contains(collection)){
			for (Collection value : collections) {
				if (value.equals(collection)) {
					value.setName(newName.strip());
				}
			}
		} else {
			throw new IllegalArgumentException("No such collection exists");
		}
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