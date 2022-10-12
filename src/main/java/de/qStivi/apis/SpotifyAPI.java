package de.qStivi.apis;


import de.qStivi.Config;
import de.qStivi.NoResultsException;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;

// TODO documentation
public class SpotifyAPI {

    private static final SpotifyApi API;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyAPI.class);

    static {
        API = new SpotifyApi.Builder().setClientId(Config.get("SPOTIFY_ID")).setClientSecret(Config.get("SPOTIFY_SECRET")).build();
        try {
            var clientCredentials = API.clientCredentials().build().execute();
            API.setAccessToken(clientCredentials.getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.error("Error while initializing Spotify API!", e);
        }

        LOGGER.info("Spotify API successfully initialized");
    }

    @NotNull
    public static Track getTrack(@NotNull String id) throws NoResultsException {
        if (id.isEmpty()) throw new IllegalArgumentException("String cannot be empty!");
        try {
            return API.getTrack(id).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new NoResultsException(e.getMessage());
        }
    }

    @NotNull
    public static Playlist getPlaylist(@NotNull String id) throws NoResultsException {
        if (id.isEmpty()) throw new IllegalArgumentException("String cannot be empty!");
        try {
            return API.getPlaylist(id).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new NoResultsException(e.getMessage());
        }
    }

    @NotNull
    public static String getIDFromLink(String link) {
        var id = link.replaceAll("https://", "");
        id = id.replaceAll("open.spotify.com/playlist/", "");
        id = id.replaceAll("open.spotify.com/track/", "");
        return id.replaceAll("\\?si=[a-z0-9]*", "");
    }
}
