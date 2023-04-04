package de.qStivi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public abstract class Properties {

    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);
    public static String DISCORD;
    public static String SPOTIFY_CLIENT_ID;
    public static String SPOTIFY_CLIENT_SECRET;
    public static String YOUTUBE_API_KEY;
    public static String TTS_API_KEY;
    public static String CHAT_API_KEY;

    static {
        var fileName = "MusicBot.properties";
        var properties = new java.util.Properties();

        try {

            LOGGER.info("Loading properties file...");
            properties.load(new FileReader(fileName));

        } catch (IOException ioException) {

            LOGGER.warn("Error while loading properties file: " + ioException.getMessage());
            LOGGER.info("Creating new one...");

            properties.setProperty("discord.bot.token", "");
            properties.setProperty("spotify.client.id", "");
            properties.setProperty("spotify.client.secret", "");
            properties.setProperty("youtube.api.key", "");
            properties.setProperty("tts.api.key", "");
            properties.setProperty("chat.api.key", "");

            try {
                properties.store(new FileWriter(fileName), "Please enter your API keys");
            } catch (IOException anotherIOException) {
                LOGGER.error("Error while creating properties file: " + anotherIOException.getMessage());
                System.exit(anotherIOException.hashCode());
            }

            LOGGER.info("New properties file has been created. Please enter your API keys.");
            System.exit(0);
        }

        LOGGER.info("Properties file successfully loaded. Getting properties...");

        DISCORD = properties.getProperty("discord.bot.token");
        SPOTIFY_CLIENT_ID = properties.getProperty("spotify.client.id");
        SPOTIFY_CLIENT_SECRET = properties.getProperty("spotify.client.secret");
        YOUTUBE_API_KEY = properties.getProperty("youtube.api.key");
        TTS_API_KEY = properties.getProperty("tts.api.key");
        CHAT_API_KEY = properties.getProperty("chat.api.key");

        if (DISCORD == null || DISCORD.isEmpty() || SPOTIFY_CLIENT_SECRET == null || SPOTIFY_CLIENT_SECRET.isEmpty() || SPOTIFY_CLIENT_ID == null || SPOTIFY_CLIENT_ID.isEmpty() || YOUTUBE_API_KEY == null || YOUTUBE_API_KEY.isEmpty() || TTS_API_KEY.isEmpty() || CHAT_API_KEY.isEmpty()) {
            LOGGER.error("Error while getting properties! Some properties are empty.");
            System.exit(-1);
        }

        LOGGER.info("All properties successfully loaded.");
//        LOGGER.info(properties.getProperty("youtube.api.key"));
    }

}
