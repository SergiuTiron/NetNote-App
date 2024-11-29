/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import commons.Note;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

public class ServerUtils {

	private static final String SERVER = "http://localhost:8080/";

	public List<Note> getNotes() {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("api/notes")
				.request(APPLICATION_JSON)
				.get(new GenericType<>() {});
	}

	public Note newEmptyNote() {
		Note emptyNote = new Note("");
		return this.addNote(emptyNote);
	}

	public Note addNote(Note note) {
		return ClientBuilder.newClient(new ClientConfig())
				.target(SERVER).path("api/notes")
				.request(APPLICATION_JSON)
				.post(Entity.entity(note, APPLICATION_JSON), Note.class);
	}

	public boolean isServerAvailable() {
		try {
			ClientBuilder.newClient(new ClientConfig())
					.target(SERVER)
					.request(APPLICATION_JSON)
					.get();
		} catch (ProcessingException e) {
			if (e.getCause() instanceof ConnectException) {
				return false;
			}
		}
		return true;
	}

	public void deleteNoteFromServer(long id) throws IOException {
		URL url = new URL(SERVER + "api/notes/" + id);
		HttpURLConnection connection = null;

		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("DELETE");

			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
				throw new RuntimeException("Failed to delete note. " + responseCode);
			}
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}