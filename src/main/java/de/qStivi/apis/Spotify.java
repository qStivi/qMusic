package de.qStivi.apis;


import de.qStivi.Config;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class Spotify {
    private static final String CLIENT_ID = Config.get("SPOTIFY_ID");
    private static final String CLIENT_SECRET = Config.get("SPOTIFY_SECRET");
    private static final SpotifyApi API = new SpotifyApi.Builder()
            .setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET)
            .build();
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * Initializes the Spotify API.
     * <p>
     * Verifies that all credentials are right.
     */
    public Spotify() throws IOException, SpotifyWebApiException, ParseException {
        final var clientCredentialsRequest = API.clientCredentials().build();
        final var clientCredentials = clientCredentialsRequest.execute();

        API.setAccessToken(clientCredentials.getAccessToken());

        LOGGER.info("Spotify loaded!");
    }

    /**
     * Get the name of a track on Spotify.
     * <p>
     * Connects to the Spotify API to get the track name corresponding to the given track id.
     *
     * @param id The track id which is included in the song link and the Spotify URI.
     * @return String containing the name of the corresponding Song. Or NULL when an error acures such as when the id is empty or NULL.
     */
    @Nullable
    @org.jetbrains.annotations.Nullable
    @CheckForNull
    public String getTrackName(String id) {
        if (id == null || id.isEmpty()) return null;
        final Track track;
        try {
            track = API.getTrack(id).build().execute();
        } catch (Exception e) {
            return null;
        }
        final var name = track.getName();
        LOGGER.info("Got Sptify track name: " + name);
        return name;
    }

    /**
     * Get artists of a Spotify track.
     * <p>
     * Connect to the Spotify API to get artists names of the song corresponding to the given id.
     *
     * @param id The track id which is included in the song link and the Spotify URI.
     * @return String[] - containing the name of the corresponding Song. Or NULL when an error acures such when the id is empty.
     */
    public String[] getTrackArtists(String id) {
        if (id == null || id.isEmpty()) return null;
        final ArtistSimplified[] artistsSimplified;
        try {
            artistsSimplified = API.getTrack(id).build().execute().getArtists();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return null;
        }
        var names = new String[artistsSimplified.length];
        for (int i = 0, artistsSimplifiedLength = artistsSimplified.length; i < artistsSimplifiedLength; i++) {
            ArtistSimplified artistSimplified = artistsSimplified[i];
            names[i] = artistSimplified.getName();
        }
        return names;
    }

    /**
     * Returns a {@link List<String>} of Strings where each entry contains the name and artist(s) of a song in the following format: song+Name+artist1+name+Artist2+name... Example: Heathens+Twenty+One+Pilots
     *
     * @param id The playlist id which is included in the playlist link and the Spotify URI.
     * @return A {@link List<String>} of Strings.
     */
    // https://developer.spotify.com/console/get-playlist-tracks/?playlist_id=3cEYpjA9oz9GiPac4AsH4n&market=ES&fields=items(added_by.id%2Ctrack(name%2Chref%2Calbum(name%2Chref)))&limit=10&offset=5&additional_types=
    public String[] getFormattedPlaylist(String id) {
        if (id == null || id.isEmpty()) return null;
        PlaylistTrack[] tracks;
        try {
            tracks = API.getPlaylist(id).build().execute().getTracks().getItems();
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            LOGGER.info("Error while trying to get tracks for playlist id: " + id);
            return null;
        }
        var output = new String[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            var trackID = tracks[i].getTrack().getId();
            var artists = getTrackArtists(trackID);
            StringBuilder artistsCombined = new StringBuilder();
            for (String artist : artists) {
                artistsCombined.append("+").append(artist.replace(" ", "+"));
            }
            output[i] = tracks[i].getTrack().getName() + artistsCombined;
        }

        return output;
    }
}