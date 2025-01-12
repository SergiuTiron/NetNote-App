package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Collection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
	public static final Path CONFIG_FILE_PATH = Paths.get("config.json");
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**Save the Config object to a JSON file
	@param config - config to save
	@throws IOException - if file is not found*/
	public void saveConfig(Config config) throws IOException {// Serialize the Config object to JSON
		String configJson = objectMapper.writeValueAsString(config);

		// Write the JSON string to the config file
		Files.write(CONFIG_FILE_PATH, configJson.getBytes());
	}


	/**Load the Config object from the JSON file
	@return - the config read
	@throws IOException - if file is not found*/
	public Config loadConfig() throws IOException {
		File configFile = new File(String.valueOf(CONFIG_FILE_PATH));
		if (!configFile.exists()) { // If file doesn't exist, return default config
			Config defaultConfig = new Config();
			saveConfig(defaultConfig); // Save it so the file gets created
			return defaultConfig;
		}

		// If the file exists, read it using Jackson
		Config config = objectMapper.readValue(configFile, Config.class);
		System.out.println("Loaded collections: " + config.getCollections().size());
		if (config.getCollections().isEmpty()) {
			System.out.println("No collections found in config.");
		} else {
			for (Collection collection : config.getCollections()) {
				System.out.println("Collection: " + collection.getName());
			}
		}

		return config;
	}
}