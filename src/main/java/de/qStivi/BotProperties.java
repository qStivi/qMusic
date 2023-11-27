package de.qStivi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BotProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotProperties.class);
    private static final String MUSIC_BOT_PROPERTIES = "MusicBot.properties";
    private static final String DISCORD_BOT_TOKEN = "discord.bot.token";
    private static final String SPOTIFY_CLIENT_ID = "spotify.client.id";
    private static final String SPOTIFY_CLIENT_SECRET = "spotify.client.secret";
    private static final String YOUTUBE_API_KEY = "youtube.api.key";
    private static final String TTS_API_KEY = "tts.api.key";
    private static final String CHAT_API_KEY = "chat.api.key";

    private final Map<String, String> properties = new HashMap<>();
    private boolean isInitialized = false;

    private BotProperties() {
        // Constructor now only responsible for instantiation.
    }

    public static BotProperties getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // Initialize properties here instead of constructor
    public void init() {
        if (isInitialized) {
            return;
        }
        var fileName = MUSIC_BOT_PROPERTIES;
        java.util.Properties props = new java.util.Properties();

        try {
            LOGGER.info("Loading properties file...");
            try (var reader = new FileReader(fileName)) {
                props.load(reader);
            }

            populateProperties(props);

        } catch (IOException ioException) {
            LOGGER.warn("Error while loading properties file: " + ioException);
            LOGGER.info("Creating new one...");

            props = createDefaultProperties();

            try (var writer = new FileWriter(fileName)) {
                props.store(writer, "Please enter your API keys");
            } catch (IOException anotherIOException) {
                LOGGER.error("Error while creating properties file: " + anotherIOException);
                System.exit(anotherIOException.hashCode());
            }
            LOGGER.info("New properties file has been created. Please enter your API keys.");
            System.exit(0);
        }
        isInitialized = true;
    }

    public String getProperty(String name) {
        var value = properties.get(name);
        ensurePropertySet(value, name);
        return value;
    }

    private void ensurePropertySet(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not set in the properties file!");
        }
    }

    private void populateProperties(java.util.Properties props) {
        if (props == null) {
            throw new IllegalArgumentException("Properties cannot be null");
        }

        putProperty(DISCORD_BOT_TOKEN, props);
        putProperty(SPOTIFY_CLIENT_ID, props);
        putProperty(SPOTIFY_CLIENT_SECRET, props);
        putProperty(YOUTUBE_API_KEY, props);
        putProperty(TTS_API_KEY, props);
        putProperty(CHAT_API_KEY, props);
        LOGGER.info("All properties successfully loaded.");
    }

    private void putProperty(String propertyKey, java.util.Properties props) {
        String value = props.getProperty(propertyKey);
        if (value == null) {
            throw new IllegalArgumentException("Property " + propertyKey + " is missing in the properties file.");
        }
        properties.put(propertyKey, value);
    }

    private java.util.Properties createDefaultProperties() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty(DISCORD_BOT_TOKEN, "");
        props.setProperty(SPOTIFY_CLIENT_ID, "");
        props.setProperty(SPOTIFY_CLIENT_SECRET, "");
        props.setProperty(YOUTUBE_API_KEY, "");
        props.setProperty(TTS_API_KEY, "");
        props.setProperty(CHAT_API_KEY, "");
        return props;
    }

    private static class SingletonHolder {
        private static final BotProperties INSTANCE = new BotProperties();

        static {
            INSTANCE.init();
        }
    }
}
